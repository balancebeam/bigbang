package io.anyway.bigbang.gateway.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.gateway.gray.GrayStrategyEvent;
import io.anyway.bigbang.gateway.service.impl.RequestPathBlackListServiceImpl;
import io.anyway.bigbang.gateway.service.impl.RequestPathWhiteListServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Configuration
public class RequestPathConfigure {

    @Value("${spring.cloud.gateway.request-path.black-dataId:gateway-black-list}")
    private String blackDataId;

    @Value("${spring.cloud.gateway.request-path.white-dataId:gateway-white-list}")
    private String whiteDataId;

    @Value("${spring.cloud.gateway.request-path.group:DEFAULT_GROUP}")
    private String group;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void init() {
        try {
            ConfigService configService = NacosFactory.createConfigService(serverAddr);

            String whiteListInfo = configService.getConfig(whiteDataId, group, 5000);
            if(StringUtils.isEmpty(whiteListInfo)){
                whiteListInfo= "";
            }
            applicationEventPublisher.publishEvent(new GrayStrategyEvent(whiteListInfo));

            String backListInfo = configService.getConfig(blackDataId, group, 5000);
            if(StringUtils.isEmpty(backListInfo)){
                backListInfo= "";
            }
            applicationEventPublisher.publishEvent(new GrayStrategyEvent(backListInfo));

            // Add listener
            configService.addListener(whiteDataId, group, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String whiteListInfo) {
                if(StringUtils.isEmpty(whiteListInfo)){
                    whiteListInfo= "";
                }
                applicationEventPublisher.publishEvent(new RequestPathWhiteListServiceImpl.WhiteListEvent(whiteListInfo));
                }
            });

            configService.addListener(blackDataId, group, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String backListInfo) {
                if(StringUtils.isEmpty(backListInfo)){
                    backListInfo= "";
                }
                applicationEventPublisher.publishEvent(new RequestPathBlackListServiceImpl.BlackListEvent(backListInfo));
                }
            });
        } catch (NacosException e) {
            log.error("init request path error", e);
        }
    }
}
