package io.anyway.bigbang.framework.logging;

import ch.qos.logback.classic.PatternLayout;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.Properties;

public interface LoggingExtensionConverterManager {

    static Properties getExtensionProperties() {

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Properties properties = new Properties();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/logging/logback-converter.properties");
            for (Resource each : resources) {
                try (InputStream in = each.getInputStream()) {
                    properties.load(in);
                }
            }
            ClassLoader classLoader= Thread.currentThread().getContextClassLoader();
            for (String each : properties.stringPropertyNames()) {
                String className= properties.getProperty(each);
                //if the value is the express, not a validated converter class
                if(className.contains("%")){
                    continue;
                }
                properties.put(each, "%"+each);
                Class clazz= classLoader.loadClass(className);
                PatternLayout.defaultConverterMap.put(each, className);
                if(InheritableThreadClassicConverter.class.isAssignableFrom(clazz) ){
                    InheritableThreadAsyncAppender.addInheritableThreadClassicConverter((InheritableThreadClassicConverter)clazz.getDeclaredConstructor().newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
