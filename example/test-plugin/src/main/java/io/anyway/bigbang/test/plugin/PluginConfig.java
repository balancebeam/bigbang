package io.anyway.bigbang.test.plugin;

import io.anyway.bigbang.test.plugin.controller.PluginController;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;


@MapperScan(basePackages="io.anyway.bigbang.test.plugin.dao")
public class PluginConfig {

    @Bean(name="helloworld")
    public HelloWorld createHelloWorld(){
        return new HelloWorld();
    }

    @Bean
    public PluginTenantService createPluginTenantService(){
        return new PluginTenantService();
    }

    @Bean
    public PluginController createHelloController(){
        return new PluginController();
    }
}
