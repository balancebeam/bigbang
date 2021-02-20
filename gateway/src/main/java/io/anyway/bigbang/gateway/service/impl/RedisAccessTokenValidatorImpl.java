package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "redis")
public class RedisAccessTokenValidatorImpl implements AccessTokenValidator {

    @Override
    public Optional<UserDetailContext> check(String accessToken) {
        return null;
    }
}
