package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;
import io.anyway.bigbang.framework.mq.service.MqMessageRouteForward;
import io.anyway.bigbang.framework.utils.JsonUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import static io.anyway.bigbang.framework.gray.GrayContext.GRAY_NAME;

@Slf4j
public class MqMessageRouteForwardImpl implements MqMessageRouteForward {

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    @Resource(name="mqLoadBalanceRestTemplate")
    private RestTemplate restTemplate;

    @Override
    public boolean isNeededForward(GrayContext grayContext) {
        if(grayContext!= null){
            if(!grayContext.getInVers().contains(discoveryMetadataService.getVersion())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void doRouteForward(MessageListenerInbound messageListenerInbound, MessageListenerDefinition messageListenerDefinition) {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        requestHeaders.add("MQ_TYPE_CODE", messageListenerInbound.getMqType().getCode());
        requestHeaders.add("CONSUMER_DEFINITION_ID", messageListenerDefinition.getId());
        requestHeaders.add(GRAY_NAME, JsonUtil.fromObject2String(messageListenerInbound.getMessageHeader().getGrayContext()));
        HttpEntity<MessageListenerInbound> httpEntity = new HttpEntity<>(messageListenerInbound, requestHeaders);
        String url = Joiner.on("").join("http://", discoveryMetadataService.getServiceId(), "/mq/producer/route/forward");
        log.info("mq forward url: {}", url);
        ResponseEntity<String> httpResult = restTemplate.postForEntity(url, httpEntity, String.class);
        log.info("mq forward response: {}", httpResult);
        String result = httpResult.getBody();
        if ("OK".equals(result)) {
            String errorMessage = String.format(
                    "The message request=%s has not been successfully processed " +
                            "and waiting for the next chance to retry.", messageListenerInbound);
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
