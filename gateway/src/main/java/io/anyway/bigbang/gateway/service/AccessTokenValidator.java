package io.anyway.bigbang.gateway.service;

import io.anyway.bigbang.framework.session.UserDetailContext;

import java.util.Optional;

public interface AccessTokenValidator {

    Optional<UserDetailContext> check(String accessToken);
}
