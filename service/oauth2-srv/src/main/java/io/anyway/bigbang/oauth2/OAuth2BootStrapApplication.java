package io.anyway.bigbang.oauth2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class OAuth2BootStrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuth2BootStrapApplication.class, args);
    }
}
