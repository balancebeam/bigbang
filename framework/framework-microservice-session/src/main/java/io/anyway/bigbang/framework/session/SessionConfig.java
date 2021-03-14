package io.anyway.bigbang.framework.session;

import io.anyway.bigbang.framework.bootstrap.HeaderContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfig {

    @Bean
    public HeaderContext createUserDetailHeaderContext(){

        return new HeaderContext() {

            @Override
            public String getName() {
                return UserDetailContext.USER_HEADER_NAME;
            }

            public void removeThreadLocal() {
                SessionContextHolder.removeUserDetailContext();
            }
        };
    }


}
