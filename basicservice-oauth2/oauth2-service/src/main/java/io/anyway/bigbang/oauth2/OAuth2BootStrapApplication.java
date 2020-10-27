package io.anyway.bigbang.oauth2;

import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class OAuth2BootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(OAuth2BootStrapApplication.class, args);
    }
}
