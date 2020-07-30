package io.anyway.bigbang.test.plugin;

import io.anyway.bigbang.test.plugin.dao.PluginDao;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

public class HelloWorld {

    @Value("${abc}")
    private String abc;

    @Resource
    private PluginDao tenantTestDao;

    @PostConstruct
    public void init(){
        System.out.println(tenantTestDao);
    }
}
