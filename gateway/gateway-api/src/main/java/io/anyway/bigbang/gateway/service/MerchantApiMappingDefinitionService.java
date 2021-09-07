package io.anyway.bigbang.gateway.service;

import io.anyway.bigbang.gateway.domain.ApiMappingDefinition;

import java.util.Optional;

public interface MerchantApiMappingDefinitionService {

    Optional<ApiMappingDefinition> getApiMappingDefinition(String apiCode);

}
