package io.anyway.bigbang.framework.tenant.proxy;

import io.anyway.bigbang.framework.tenant.domain.Tenant;
import io.anyway.bigbang.framework.tenant.service.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class PluginManagement {

    @Resource
    private ApplicationContext parentApplicationContext;

    @Autowired(required = false)
    private List<PluginComponentClassBuilder> pluginComponentClassBuilders = Collections.emptyList();

    @Resource
    private TenantService tenantService;

    final private ConcurrentHashMap<String,AnnotationConfigApplicationContext> tenantApplicationContextMapping = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() throws Exception {
        for(String each: tenantService.getTenantIds()){
            Tenant tenant= tenantService.getTenantById(each);
            createTenantApplicationContext(tenant.getName(),tenant.getClasspathUrls());
        }
    }

    @PreDestroy
    public void destroy(){
        for(AnnotationConfigApplicationContext each: tenantApplicationContextMapping.values()){
            each.close();
        }
    }

    public AnnotationConfigApplicationContext getApplicationContext(String tenantId){
        return tenantApplicationContextMapping.get(tenantId);
    }

    private void createTenantApplicationContext(String tenantId,List<String> paths) throws Exception {
        if (paths == null || paths.isEmpty()) {
            return;
        }
        URL[] urls = new URL[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            urls[i] = new File(paths.get(i)).toURI().toURL();
        }
        URLClassLoader urlClassLoader = new PluginURLClassLoader(urls, getClass().getClassLoader());
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ClassPathResource resource = new ClassPathResource("plugin.yml", urlClassLoader);
        if (resource.exists()) {
            List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("plugin.yml", resource);
            ctx.getEnvironment().getPropertySources().addLast(sources.get(0));
            log.info("Init tenant {} YamlPropertySource: {}", tenantId, sources);
        }
        String[] activeProfiles = parentApplicationContext.getEnvironment().getActiveProfiles();
        for (String each : activeProfiles) {
            resource = new ClassPathResource("plugin-" + each + ".yml", urlClassLoader);
            if (resource.exists()) {
                List<PropertySource<?>> sources = new YamlPropertySourceLoader().load("plugin-" + each + ".yml", resource);
                ctx.getEnvironment().getPropertySources().addLast(sources.get(0));
                log.info("Init tenant {} YamlPropertySource: {}", tenantId, sources);
            }
        }
        ctx.setDisplayName("tenant-" + tenantId);
        ctx.setParent(parentApplicationContext);
        ctx.setClassLoader(urlClassLoader);
        for (PluginComponentClassBuilder each : pluginComponentClassBuilders) {
            Class<?> componentClass = each.build(ctx);
            if (componentClass != null) {
                ctx.register(componentClass);
                log.info("Init tenant {} componentClass: {} ", tenantId, componentClass);
            }
        }
        URL url = urlClassLoader.findResource("META-INF/spring.factories");
        if (url != null) {
            Properties properties = new Properties();
            try (InputStream in = url.openStream()) {
                properties.load(in);
            }
            String autoComponentClasses = (String) properties.get("org.springframework.boot.autoconfigure.EnableAutoConfiguration");
            for (String each : autoComponentClasses.split(",")) {
                Class<?> componentClass = urlClassLoader.loadClass(each);
                ctx.register(componentClass);
                log.info("Init tenant {} componentClass: {} ", tenantId, componentClass);
            }
        }
        ctx.register(PluginWebMvcConfig.class);
        ClassLoader sourceClassLoader= Thread.currentThread().getContextClassLoader();
        try{
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            ctx.refresh();
        }finally{
            Thread.currentThread().setContextClassLoader(sourceClassLoader);
        }
        log.info("Init tenant {} ApplicationContext: {}",tenantId,ctx);
        tenantApplicationContextMapping.put(tenantId,ctx);
    }
}
