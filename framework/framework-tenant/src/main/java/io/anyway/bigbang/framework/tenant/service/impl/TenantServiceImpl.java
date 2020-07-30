package io.anyway.bigbang.framework.tenant.service.impl;

import io.anyway.bigbang.framework.tenant.domain.Tenant;
import io.anyway.bigbang.framework.tenant.property.TenantProperties;
import io.anyway.bigbang.framework.tenant.service.TenantService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
public class TenantServiceImpl implements TenantService {

    private ConcurrentHashMap<String,Tenant> mapping= new ConcurrentHashMap<>();

    @Resource
    private TenantProperties tenantProperties;

    @Override
    public Tenant getTenant(String token) {
        return mapping.get(token);
    }

    @Override
    public Tenant getTenantById(String tenantId) {
        return tenantProperties.getTenantMetadataMapping().get(tenantId);
    }

    @Override
    public Collection<String> getTenantIds() {
        return tenantProperties.getTenantMetadataMapping().keySet();
    }

    @Override
    public String getDefaultTenantId() {
        return tenantProperties.getDefaultTenant();
    }

    @PostConstruct
    public void init(){
        for(Map.Entry<String,Tenant> each: tenantProperties.getTenantMetadataMapping().entrySet()){
            each.getValue().setName(each.getKey());
            String token =each.getValue().getToken();
            if(!StringUtils.isEmpty(token)) {
                mapping.put(each.getValue().getToken(), each.getValue());
            }
        }
        log.info("Tenant Mapping: {}",mapping);
    }

}
