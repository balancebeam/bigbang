package io.anyway.bigbang.framework.logging.config;

import io.anyway.bigbang.framework.logging.marker.LoggingMarkerAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingConfigure {

    @Bean
    LoggingMarkerAspect createLoggingMarkerAspect(){
        return new LoggingMarkerAspect();
    }
}
