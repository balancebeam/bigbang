package io.anyway.bigbang.framework.core.logging;

import ch.qos.logback.classic.PatternLayout;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface LoggingExtensionManager {

    static Properties getExtensionProperties(){

        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver= new PathMatchingResourcePatternResolver();
        Properties properties= new Properties();
        try {
            Resource[] resources= pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/bigbang-logback.properties");
            for(Resource each: resources){
                try(InputStream in= each.getInputStream()){
                    properties.load(in);
                }
            }
            for(String each: properties.stringPropertyNames()){
                PatternLayout.defaultConverterMap.put(each,properties.getProperty(each));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
