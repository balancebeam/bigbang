package io.anyway.bigbang.framework.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

@Configuration
public class IdGenerator implements InitializingBean {

    private static IdGenerator ID_GENERATOR;

    private IdWorker idWorker;

    @Value("${spring.application.name}")
    private String appName;

    public static String next() {
        String id =ID_GENERATOR.idWorker.getId()+"";
        return id;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String id= InetAddress.getLocalHost()+":"+appName+":"+Math.random();
        long idepoch= System.identityHashCode(id);
        idWorker= new IdWorker(idepoch);
        ID_GENERATOR= this;
    }
}
