package io.anyway.bigbang.framework.kernel.useragent;

import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.anyway.bigbang.framework.kernel.useragent.UserAgent.USER_AGENT_NAME;

@Configuration
public class UserAgentConfig {

    @Bean
    public PrincipleHeaderKey createLocaleHeaderKey(){
        return () -> "Accept-Language";
    }

    @Bean
    public PrincipleHeaderKey createUserAgentHeaderKey(){

        return new PrincipleHeaderKey() {
            public String name() {
                return USER_AGENT_NAME;
            }

            public void removeThreadLocal() {
                UserAgentContext.threadLocal.remove();
            }
        };
    }
}
