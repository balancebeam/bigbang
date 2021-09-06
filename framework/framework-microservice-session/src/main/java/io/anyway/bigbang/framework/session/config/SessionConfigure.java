package io.anyway.bigbang.framework.session.config;

import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.session.DefaultUserDetailContext;
import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.framework.session.UserDetailExtDefinition;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class SessionConfigure implements SmartInitializingSingleton {

    @Resource
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public UserDetailExtDefinition createDefaultUserDetailDefinition(){
        return () -> DefaultUserDetailContext.class;
    }

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

    @Override
    public void afterSingletonsInstantiated() {
        SessionContextHolder.setUserDetailClass(applicationContext.getBean(UserDetailExtDefinition.class).def());
    }
}
