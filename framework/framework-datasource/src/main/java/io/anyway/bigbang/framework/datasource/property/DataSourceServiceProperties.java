package io.anyway.bigbang.framework.datasource.property;

import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.bigbang.datasource")
public class DataSourceServiceProperties{
    private Map<String,DataSourceWrapper> tenantDatasourceConfig= Collections.EMPTY_MAP;
}
