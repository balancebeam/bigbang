package io.anyway.bigbang.framework.core.security;

import io.anyway.bigbang.framework.core.concurrent.InheritableThreadProcessor;
import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@Slf4j
public class SecurityContextConfig {

    @Value("${spring.bigbang.security.user-header-name:X-AUTH-USER}")
    private String USER_HEADER_NAME;

    @Bean
    public InheritableThreadProcessor userDetailInheritableThreadProcessor(){
        log.debug("init userDetailInheritableThreadProcessor");
        return new InheritableThreadProcessor<UserDetail>() {

            @Override
            public UserDetail getInheritableThreadValue() {
                return SecurityContextHolder.getUserDetail();
            }

            @Override
            public void setInheritableThreadValue(UserDetail userDetail) {
                SecurityContextHolder.setUserDetail(userDetail);
            }
            @Override
            public void removeInheritableThreadValue() {
                SecurityContextHolder.remove();
            }

        };
    }

    @Bean
    @ConditionalOnMissingBean(name="userDetailHeaderDeliveryInterceptor")
    public HeaderDeliveryInterceptor userDetailHeaderDeliveryInterceptor(){
        log.info("init UserDetailHeaderDeliveryInterceptor");
        return headers -> {
            if(SecurityContextHolder.getUserDetail()!= null) {
                headers.put(USER_HEADER_NAME, SecurityContextHolder.getUserDetail().toJson());
            }
        };
    }

}
