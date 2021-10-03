package io.anyway.bigbang.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.gateway.service.impl.RequestPathBlackListServiceImpl;
import io.anyway.bigbang.gateway.service.impl.RequestPathWhiteListServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Properties;

@Slf4j
@Configuration
public class RequestPathConfigure implements SmartInitializingSingleton {

    @Value("${spring.cloud.gateway.request-path.black-dataId:gateway-black-list}")
    private String blackDataId;

    @Value("${spring.cloud.gateway.request-path.white-dataId:gateway-white-list}")
    private String whiteDataId;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            Properties properties = new Properties();
            if(!StringUtils.isEmpty(nacosConfigProperties.getNamespace())){
                properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNamespace());
            }
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
            properties.put("fileExtension","text");
            if(!StringUtils.isEmpty(nacosConfigProperties.getUsername())){
                properties.put(PropertyKeyConst.USERNAME,nacosConfigProperties.getUsername());
                properties.put(PropertyKeyConst.PASSWORD,nacosConfigProperties.getPassword());
            }

            ConfigService configService = NacosFactory.createConfigService(properties);
            //white list
            String whiteListInfo =configService.getConfigAndSignListener(whiteDataId, nacosConfigProperties.getGroup(), 5000, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String whiteListInfo) {
                    if(StringUtils.isEmpty(whiteListInfo)){
                        whiteListInfo= "";
                    }
                    applicationEventPublisher.publishEvent(new RequestPathWhiteListServiceImpl.WhiteListEvent(whiteListInfo));
                }
            });
            if(StringUtils.isEmpty(whiteListInfo)){
                whiteListInfo= "";
            }
            applicationEventPublisher.publishEvent(new RequestPathWhiteListServiceImpl.WhiteListEvent(whiteListInfo));
            //black list
            String backListInfo =configService.getConfigAndSignListener(blackDataId, nacosConfigProperties.getGroup(), 5000, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String backListInfo) {
                    if(StringUtils.isEmpty(backListInfo)){
                        backListInfo= "";
                    }
                    applicationEventPublisher.publishEvent(new RequestPathBlackListServiceImpl.BlackListEvent(backListInfo));
                }
            });
            if(StringUtils.isEmpty(backListInfo)){
                backListInfo= "";
            }
            applicationEventPublisher.publishEvent(new RequestPathBlackListServiceImpl.BlackListEvent(backListInfo));
        } catch (NacosException e) {
            log.error("init request path error", e);
        }
    }
}
