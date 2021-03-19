package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.ApiPrivilegeValidatorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.authentication-validator",name="enabled",havingValue = "true")
public class ApiPrivilegeValidatorServiceImpl implements ApiPrivilegeValidatorService {

    @Override
    public boolean permit(String serviceCode) {

        Optional<UserDetailContext> userDetailContext= SessionContextHolder.getUserDetailContext();
        if(userDetailContext.isPresent()) {
            //TODO read user's privilege from local cache/redis/other storage
        }
        return true;
    }
}
