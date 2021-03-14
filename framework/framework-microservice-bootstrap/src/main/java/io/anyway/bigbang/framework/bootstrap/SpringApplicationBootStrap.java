package io.anyway.bigbang.framework.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.util.*;

public class SpringApplicationBootStrap {

    public static void run(Class<?> primarySource, String... args){
        SpringApplication springApplication= new SpringApplication(primarySource){
            protected void configureProfiles(ConfigurableEnvironment environment, String[] args) {
                super.configureProfiles(environment,args);
            }
        };
        springApplication.run(args);
    }

}
