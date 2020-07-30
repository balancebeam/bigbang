package io.anyway.bigbang.framework.tenant.service;


import io.anyway.bigbang.framework.tenant.domain.Tenant;

import java.util.Collection;

public interface TenantService {

    Tenant getTenant(String token);

    Tenant getTenantById(String tenantId);

    Collection<String> getTenantIds();

    String getDefaultTenantId();

}
