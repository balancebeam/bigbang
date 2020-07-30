package io.anyway.bigbang.framework.kernel.logging;

import ch.qos.logback.classic.PatternLayout;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.Properties;

public interface LoggingExtensionManager {

    static Properties getExtensionProperties() {

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Properties properties = new Properties();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/logback-extension.properties");
            for (Resource each : resources) {
                try (InputStream in = each.getInputStream()) {
                    properties.load(in);
                }
            }
            ClassLoader classLoader= Thread.currentThread().getContextClassLoader();
            for (String each : properties.stringPropertyNames()) {
                String className= properties.getProperty(each);
                Class clazz= classLoader.loadClass(className);
                PatternLayout.defaultConverterMap.put(each, className);
                if(clazz.isAssignableFrom(InheritableThreadClassicConverter.class)){
                    InheritableThreadAsyncAppender.addInheritableThreadClassicConverter((InheritableThreadClassicConverter)clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }
}
