package io.anyway.bigbang.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayBootStrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayBootStrapApplication.class, args);
    }
}
