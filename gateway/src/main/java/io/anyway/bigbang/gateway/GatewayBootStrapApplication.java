package io.anyway.bigbang.gateway;

import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GatewayBootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(GatewayBootStrapApplication.class, args);
    }
}
