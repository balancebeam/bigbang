package io.anyway.bigbang.framework.mqclient.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import io.anyway.bigbang.framework.discovery.GrayRouteContextHolder;
import io.anyway.bigbang.framework.mqclient.domain.MessageHeader;
import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;
import io.anyway.bigbang.framework.mqclient.domain.RestHeader;
import io.anyway.bigbang.framework.mqclient.service.MqRequestRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.GRAY_ROUTE_NAME;


@Slf4j
@Configuration
public class RouterConfig {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @LoadBalanced
    @Bean("lbRestTemplate")
    public RestTemplate lbRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    @Bean
    public MqRequestRouter createMqRequestRouter(@Qualifier("lbRestTemplate") RestTemplate restTemplate) {
        return mqClientMessage -> {
            MessageHeader messageHeader = mqClientMessage.getMessageHeader();
            log.info("Routing with MessageHeader={}, message_key={}, message_id={}",
                    messageHeader,
                    mqClientMessage.getMessageKey(),
                    mqClientMessage.getMessageId());
            if (messageHeader == null ||
                    messageHeader.getGrayRouteContext() == null ||
                    messageHeader.getGrayRouteContext().getCluster().equals(nacosDiscoveryProperties.getClusterName())){
                return false;
            }

            try {
                GrayRouteContextHolder.setGrayRouteContext(messageHeader.getGrayRouteContext());
                HttpHeaders requestHeaders = new HttpHeaders();
                requestHeaders.add(GRAY_ROUTE_NAME, JSONObject.toJSONString(messageHeader.getGrayRouteContext()));
                HttpEntity<MqClientMessage> httpEntity = new HttpEntity<>(mqClientMessage, requestHeaders);
                String url = Joiner.on("").join("http://", nacosDiscoveryProperties.getService(), "/mq/routing");
                log.info("redirect url: {}", url);
                ResponseEntity<RestHeader> httpResult = restTemplate.postForEntity(url, httpEntity, RestHeader.class);
                log.info("redirect response: {}", httpResult);
                RestHeader result = httpResult.getBody();
                if (result.getErrorCode() != 0) {
                    String errorMessage = String.format(
                            "The message request=%s has not been successfully processed " +
                                    "and waiting for the next chance to retry.", mqClientMessage);
                    log.error(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
                return true;
            } catch (Exception e) {
                log.warn("Exception happen when routing messages, follow the default logic", e);
                return false;
            }
            finally {
                GrayRouteContextHolder.removeGrayRouteContext();
            }
        };
    }
}
