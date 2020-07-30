package io.anyway.bigbang.framework.tenant.autoconfigure;

import io.anyway.bigbang.framework.core.concurrent.InheritableThreadProcessor;
import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryInterceptor;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import io.anyway.bigbang.framework.tenant.property.TenantProperties;
import io.anyway.bigbang.framework.tenant.proxy.ProxyServiceAutoConfiguredScannerRegistrar;
import io.anyway.bigbang.framework.tenant.proxy.PluginManagement;
import io.anyway.bigbang.framework.tenant.service.TenantService;
import io.anyway.bigbang.framework.tenant.service.impl.TenantServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@ImportAutoConfiguration({ProxyServiceAutoConfiguredScannerRegistrar.class})
@EnableConfigurationProperties(TenantProperties.class)
public class TenantContextConfiguration {

    @Bean
    public InheritableThreadProcessor tenantDetailInheritableThreadProcessor(){
        log.debug("init tenantDetailInheritableThreadProcessor");
        return new InheritableThreadProcessor<TenantDetail>() {

            @Override
            public TenantDetail getInheritableThreadValue() {
                return TenantContextHolder.getTenantDetail();
            }

            @Override
            public void setInheritableThreadValue(TenantDetail tenantDetail) {
                TenantContextHolder.setTenantDetail(tenantDetail);
            }

            @Override
            public void removeInheritableThreadValue() {
                TenantContextHolder.remove();
            }

        };
    }

    @Bean
    @ConditionalOnMissingBean(name="tenantHeaderDeliveryInterceptor")
    public HeaderDeliveryInterceptor tenantHeaderDeliveryInterceptor(){
        log.info("init TenantHeaderDeliveryInterceptor");
        return headers -> {
            if(TenantContextHolder.getTenantDetail()!= null) {
                headers.put(TenantDetail.HEADER_TENANT_KEY, TenantContextHolder.getTenantDetail().getTenantId());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TenantService createDefaultTenantService(){
        return new TenantServiceImpl();
    }

    @Bean
    public PluginManagement createTenantManagement(){
        return new PluginManagement();
    }
}
