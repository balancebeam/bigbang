package io.anyway.bigbang.gateway;

import io.anyway.bigbang.framework.autoconfigure.MicroserviceAutoConfigure;
import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import io.anyway.bigbang.framework.bootstrap.config.RestTemplateConfigure;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@ImportAutoConfiguration({RestTemplateConfigure.class})
@SpringBootApplication(exclude={MicroserviceAutoConfigure.class})
public class GatewayBootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(GatewayBootStrapApplication.class, args);
    }
}
