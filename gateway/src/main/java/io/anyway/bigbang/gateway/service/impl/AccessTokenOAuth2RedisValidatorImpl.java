package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.oauth2.domain.XUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "redis")
public class AccessTokenOAuth2RedisValidatorImpl implements AccessTokenValidator {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    private RedisTokenStore redisTokenStore;

    @PostConstruct
    public void init(){
        redisTokenStore= new RedisTokenStore(redisConnectionFactory);
    }

    @Override
    public Optional<UserDetailContext> check(String accessToken) {
        OAuth2Authentication authentication =redisTokenStore.readAuthentication(accessToken);
        if(authentication!= null) {
            XUserDetails details = (XUserDetails) authentication.getPrincipal();
            String appId = details.getAppId();
            String userId = details.getUsername();
            String username = details.getLoginName();
            String userType = details.getUserType();
            UserDetailContext userDetail = new UserDetailContext(appId,userId, username, userType);
            log.debug("redis UserDetail: {}", userDetail);
            return Optional.of(userDetail);
        }
        return Optional.empty();
    }
}
