package io.anyway.bigbang.example.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients(basePackages="io.anyway.bigbang.example.api")
@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan
public class ConsumerBootStrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerBootStrapApplication.class, args);
    }
}
