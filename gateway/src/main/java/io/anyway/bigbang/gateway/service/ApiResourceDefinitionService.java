package io.anyway.bigbang.gateway.service;


import io.anyway.bigbang.gateway.domain.ApiResource;

import java.util.Optional;


public interface ApiResourceDefinitionService {

    Optional<ApiResource> getApiResource(String serviceCode);
}
