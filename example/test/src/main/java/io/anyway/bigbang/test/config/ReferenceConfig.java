package io.anyway.bigbang.test.config;

import io.anyway.bigbang.test.HelloService;
import io.anyway.bigbang.test.ReferenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReferenceConfig {

    @Bean
    public ReferenceService createReferenceService(HelloService helloService){
        return new ReferenceService(helloService);
    }
}
