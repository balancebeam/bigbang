package io.anyway.bigbang.framework.bootstrap;

import io.anyway.bigbang.framework.gray.ApplicationEnvironmentPreparedEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringApplicationBootStrap {

    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        SpringApplication springApplication = new SpringApplication(primarySource);
        springApplication.addListeners(new ApplicationEnvironmentPreparedEventListener());
        return springApplication.run(args);
    }
}
