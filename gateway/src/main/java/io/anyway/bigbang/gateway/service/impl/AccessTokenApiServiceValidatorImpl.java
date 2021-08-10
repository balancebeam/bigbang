package io.anyway.bigbang.gateway.service.impl;

import com.djtgroup.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "api")
public class AccessTokenApiServiceValidatorImpl implements AccessTokenValidator {

    @Override
    public Optional<UserDetailContext> check(String accessToken) {
        return null;
    }
}
