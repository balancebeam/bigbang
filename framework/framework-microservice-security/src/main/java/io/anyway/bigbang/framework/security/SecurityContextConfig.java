package io.anyway.bigbang.framework.security;

import io.anyway.bigbang.framework.header.HeaderContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.anyway.bigbang.framework.security.UserDetailContext.USER_HEADER_NAME;

@Configuration
public class SecurityContextConfig {

    @Bean
    public HeaderContext createUserDetailHeaderContext(){

        return new HeaderContext() {

            @Override
            public String getName() {
                return USER_HEADER_NAME;
            }

            public void removeThreadLocal() {
                SecurityContextHolder.removeUserDetailContext();
            }
        };
    }


}
