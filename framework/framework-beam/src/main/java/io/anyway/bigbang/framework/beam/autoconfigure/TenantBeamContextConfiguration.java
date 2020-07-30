package io.anyway.bigbang.framework.beam.autoconfigure;

import io.anyway.bigbang.framework.beam.service.EnhanceContextProcessor;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@Slf4j
@AutoConfigureBefore(XLoadBalancerConfiguration.class)
@ConditionalOnClass(TenantDetail.class)
public class TenantBeamContextConfiguration {

    @Bean
    public EnhanceContextProcessor createEnhanceContextProcessor(){
        return ctx -> {
            TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
            if(tenantDetail!=null){
                String uid= ctx.getUid();
                uid= tenantDetail.getTenantId()+ (StringUtils.isEmpty(uid)?"":"_")+ uid;
                ctx.setUid(uid);
            }

            String clientId= ctx.getClientId();
            if(tenantDetail!=null){
                clientId= tenantDetail.getTenantId()+(StringUtils.isEmpty(clientId)?"":"_")+clientId;
                ctx.setClientId(clientId);
            }
        };
    }
}
