package io.anyway.bigbang.framework.useragent;

import io.anyway.bigbang.framework.bootstrap.HeaderContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.anyway.bigbang.framework.useragent.UserAgentContext.USER_AGENT_NAME;


@Configuration
public class UserAgentContextConfig {

    @Bean
    public HeaderContext createLocaleHeaderContext(){
        return () -> "Accept-Language";
    }

    @Bean
    public HeaderContext createUserAgentHeaderContext(){

        return new HeaderContext() {

            @Override
            public String getName() {
                return USER_AGENT_NAME;
            }

            public void removeThreadLocal() {
                UserAgentContextHolder.removeUserAgentContext();
            }
        };
    }
}
