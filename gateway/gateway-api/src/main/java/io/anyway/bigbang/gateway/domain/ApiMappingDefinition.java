package io.anyway.bigbang.gateway.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.cloud.gateway.route.Route;

@Getter
@Setter
@ToString
public class ApiMappingDefinition {
    private String serviceId;
    private String apiCode;
    private String path;
    private Route route;
}
