package io.anyway.bigbang.framework.gray.config;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.gray.GrayContextHolder;
import io.anyway.bigbang.framework.header.HeaderContext;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ImportAutoConfiguration({RibbonConfigure.class})
public class GrayConfigure {

    @Bean
    public HeaderContext createGrayRouteHeaderContext(){
        return new HeaderContext() {
            @Override
            public String getName() {
                return GrayContext.GRAY_NAME;
            }

            @Override
            public void removeThreadLocal() {
                GrayContextHolder.removeGrayContext();
            }
        };
    }

}
