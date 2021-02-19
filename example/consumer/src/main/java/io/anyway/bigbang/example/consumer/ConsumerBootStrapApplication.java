package io.anyway.bigbang.example.consumer;

import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@EnableFeignClients(basePackages="io.anyway.bigbang.example.api")
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScans({@ComponentScan("io.anyway.bigbang.example")})
public class ConsumerBootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(ConsumerBootStrapApplication.class, args);
    }
}
