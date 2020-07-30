package io.anyway.bigbang.framework.tenant.logging.converter;

import io.anyway.bigbang.framework.core.logging.InheritableThreadClassicConverter;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;

public class TenantConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue() {
        TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
        return tenantDetail!= null? tenantDetail.getTenantId(): "N/A";
    }
}
