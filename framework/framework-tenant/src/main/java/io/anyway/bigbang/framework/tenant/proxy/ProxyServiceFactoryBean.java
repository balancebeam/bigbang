package io.anyway.bigbang.framework.tenant.proxy;

import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import io.anyway.bigbang.framework.tenant.exception.TenantServiceNotFoundException;
import io.anyway.bigbang.framework.tenant.service.TenantService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyServiceFactoryBean<T>  implements FactoryBean<T> {

    @Getter
    @Setter
    private PluginManagement tenantManagement;

    @Getter
    @Setter
    private TenantService tenantService;

    private Class<T> tenantInterface;

    private T proxyInstance;

    public ProxyServiceFactoryBean(Class<T> tenantInterface) {
        this.tenantInterface = tenantInterface;
    }

    @Override
    public T getObject() throws Exception {
        if(proxyInstance== null) {
            proxyInstance = (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{tenantInterface}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    TenantDetail detail= TenantContextHolder.getTenantDetail();
                    String tenantId = detail!= null? detail.getTenantId(): tenantService.getDefaultTenantId();
                    ApplicationContext ctx = tenantManagement.getApplicationContext(tenantId);
                    T target;
                    if (ctx == null || (proxyInstance == (target = ctx.getBean(tenantInterface)))) {
                        throw new TenantServiceNotFoundException(tenantId + " didn't implement " + tenantInterface);
                    }
                    return method.invoke(target,args);
                }
            });
        }
        return proxyInstance;
    }

    @Override
    public Class<T> getObjectType() {
        return tenantInterface;
    }
}
