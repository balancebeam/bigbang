package io.anyway.bigbang.test.config2;

import io.anyway.bigbang.test.HelloService;
import io.anyway.bigbang.test.config.HelloConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

//@Configuration
//@AutoConfigureBefore(HelloConfig.class)
public class HelloConfig2 {

    @Bean
    public HelloService createMasterHelloService(){
        return new HelloService(){

            @Override
            public void hello() {
                System.out.println("master");
            }
        };
    }




}
