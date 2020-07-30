package io.anyway.bigbang.framework.kernel.security;

import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.anyway.bigbang.framework.kernel.security.UserDetail.USER_HEADER_NAME;

@Configuration
public class SecurityConfig {

    @Bean
    public PrincipleHeaderKey createSecurityHeaderKey(){
        return new PrincipleHeaderKey() {

            @Override
            public String name() {
                return USER_HEADER_NAME;
            }

            @Override
            public void removeThreadLocal() {
                SecurityContext.threadLocal.remove();
            }
        };
    }


}
