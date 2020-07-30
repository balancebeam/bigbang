package io.anyway.bigbang.framework.kernel.gray;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.cloud.nacos.discovery")
public class MetadataMapProperties {

    private Map<String,String> metadataMap= Collections.emptyMap();
}
