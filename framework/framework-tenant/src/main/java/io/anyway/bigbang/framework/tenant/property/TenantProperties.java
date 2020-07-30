package io.anyway.bigbang.framework.tenant.property;

import io.anyway.bigbang.framework.tenant.domain.Tenant;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * spring.bigbang.tenant.tenant-list:
 *  -id: 1
 *   token: bce7bd01-p049-4d86-86ca-11508376d4a0
 *   name: anonymous1
 *  -id: 2
 *   token: cf3cfc61-ek56-4923-917a-0f54a7960534
 *   name: anonymous2
 *  -id: 3
 *   token: 7eh5b1l6-21ad-4378-b8cd-f00f7a059061
 *   name: anonymous3
 */
@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.bigbang.tenant")
public class TenantProperties {

    private String defaultTenant;

    private Map<String,Tenant> tenantMetadataMapping = new HashMap<>();
}
