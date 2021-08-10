package io.anyway.bigbang.framework.apm;

import io.anyway.bigbang.framework.bootstrap.SpringApplicationBootStrap;
import io.anyway.bigbang.framework.logging.LoggingConverterPatternMapper;
import org.springframework.core.env.ConfigurableEnvironment;

public class TraceLoggingConverterPatternMapper implements LoggingConverterPatternMapper {

    @Override
    public String map() {
        ConfigurableEnvironment environment= SpringApplicationBootStrap.getEnvironment();
        boolean bool= "true".equals(environment.getProperty("spring.sleuth.enabled"));
        return bool? "%X{X-B3-TraceId:-}": DefaultLoggingTraceIdConverter.class.getName();
    }
}
