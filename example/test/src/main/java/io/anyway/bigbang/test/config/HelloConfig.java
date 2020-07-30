package io.anyway.bigbang.test.config;

import io.anyway.bigbang.test.HelloProperties;
import io.anyway.bigbang.test.HelloService;
import io.anyway.bigbang.test.config2.HelloConfig2;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
@AutoConfigureAfter(HelloConfig2.class)
@EnableConfigurationProperties(HelloProperties.class)
public class HelloConfig {

    @Bean
    @ConditionalOnMissingBean
    public HelloService createDefaultHelloService(){
        return new HelloService(){

            @Override
            public void hello() {
                System.out.println("default");
            }
        };
    }


}
