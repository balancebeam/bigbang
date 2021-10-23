package io.anyway.bigbang.example;

import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@ServletComponentScan
public class ProviderBootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(ProviderBootStrapApplication.class, args);
    }
}
