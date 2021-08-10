package io.anyway.bigbang.gateway.service;

import com.djtgroup.framework.session.UserDetailContext;

import java.util.Optional;

public interface AccessTokenValidator {

    Optional<UserDetailContext> check(String accessToken);
}
