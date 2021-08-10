package io.anyway.bigbang.framework.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

public class SpringApplicationBootStrap {

    private static ConfigurableEnvironment environment;

    public static void run(Class<?> primarySource, String... args){
        SpringApplication springApplication= new SpringApplication(primarySource){
            protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
                super.configureProfiles(environment,args);
                SpringApplicationBootStrap.environment= environment;
            }
        };
        springApplication.run(args);
    }

    public static ConfigurableEnvironment getEnvironment(){
        return environment;
    }

}
