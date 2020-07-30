package io.anyway.bigbang.framework.tenant;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TenantDetail {
    final public static String HEADER_TENANT_KEY= "X-Tenant-ID";
    private String tenantId;
}
