package io.anyway.bigbang.framework.tenant;

public class TenantContextHolder {

    final private static ThreadLocal<TenantDetail> context = new ThreadLocal<>();

    final public static TenantDetail getTenantDetail() {
        return context.get();
    }

    final public static void setTenantDetail(TenantDetail tenantDetail) {
        context.set(tenantDetail);
    }

    final public static void remove() {
        context.remove();
    }

}
