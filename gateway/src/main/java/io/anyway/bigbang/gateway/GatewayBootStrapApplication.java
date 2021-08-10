package io.anyway.bigbang.gateway;

import com.djtgroup.framework.autoconfigure.MicroserviceAutoConfigure;
import com.djtgroup.framework.bootstrap.SpringApplicationBootStrap;
import com.djtgroup.framework.bootstrap.config.RestTemplateConfigure;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@ImportAutoConfiguration(RestTemplateConfigure.class)
@SpringBootApplication(exclude={MicroserviceAutoConfigure.class})
public class GatewayBootStrapApplication {

    public static void main(String[] args) {
        SpringApplicationBootStrap.run(GatewayBootStrapApplication.class, args);
    }
}
