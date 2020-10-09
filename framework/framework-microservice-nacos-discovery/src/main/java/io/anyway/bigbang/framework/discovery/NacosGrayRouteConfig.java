package io.anyway.bigbang.framework.discovery;

import io.anyway.bigbang.framework.header.HeaderContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.GRAY_ROUTE_NAME;

@Configuration
public class NacosGrayRouteConfig {

    @Bean
    public HeaderContext createGrayRouteHeaderContext(){
        return new HeaderContext() {
            @Override
            public String getName() {
                return GRAY_ROUTE_NAME;
            }

            @Override
            public void removeThreadLocal() {
                GrayRouteContextHolder.removeGrayRouteContext();
            }
        };
    }

}
