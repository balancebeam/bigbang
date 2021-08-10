package io.anyway.bigbang.framework.session.config;

import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfigure {

    @Bean
    public HeaderContext createUserDetailHeaderContext() {

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
