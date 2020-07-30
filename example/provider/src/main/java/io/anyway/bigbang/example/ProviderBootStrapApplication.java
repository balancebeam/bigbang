package io.anyway.bigbang.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ProviderBootStrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderBootStrapApplication.class, args);
    }
}
