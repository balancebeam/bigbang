package io.anyway.bigbang.gateway.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.bigbang.gateway")
public class GatewayProperties {

    private List<String> ipBlackList= Collections.emptyList();

    private List<String> pathWhiteList= Collections.emptyList();
}
