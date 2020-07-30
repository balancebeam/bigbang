package io.anyway.bigbang.test;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class TenantReferenceService {

    @Resource
    private ThisTenantService thisTenantService;

    @PostConstruct
    public void init(){
        System.out.println("xxxx->"+thisTenantService.hello());
    }
}
