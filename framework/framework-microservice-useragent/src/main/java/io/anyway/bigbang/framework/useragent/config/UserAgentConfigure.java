package io.anyway.bigbang.framework.useragent.config;

import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.useragent.UserAgentContext;
import io.anyway.bigbang.framework.useragent.UserAgentContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class UserAgentConfigure {

    @Bean
    public HeaderContext createLocaleHeaderContext(){
        return () -> "Accept-Language";
    }

    @Bean
    public HeaderContext createUserAgentHeaderContext(){

        return new HeaderContext() {

            @Override
            public String getName() {
                return UserAgentContext.USER_AGENT_NAME;
            }

            public void removeThreadLocal() {
                UserAgentContextHolder.removeUserAgentContext();
            }
        };
    }
}
