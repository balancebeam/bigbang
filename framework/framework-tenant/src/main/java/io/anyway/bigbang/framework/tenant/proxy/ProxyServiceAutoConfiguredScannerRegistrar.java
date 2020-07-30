package io.anyway.bigbang.framework.tenant.proxy;

import io.anyway.bigbang.framework.core.annotation.ProxyServiceInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ProxyServiceAutoConfiguredScannerRegistrar implements
        BeanFactoryAware,
        ImportBeanDefinitionRegistrar,
        ResourceLoaderAware {

    private BeanFactory beanFactory;

    private ResourceLoader resourceLoader;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory= beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader= resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        log.debug("Searching for mappers annotated with @Tenant");

        ProxyServiceClassPathScanner scanner = new ProxyServiceClassPathScanner(registry);
        if (this.resourceLoader != null) {
            scanner.setResourceLoader(this.resourceLoader);
        }
        List<String> packages= new ArrayList<>();
        try {
            packages.addAll(AutoConfigurationPackages.get(this.beanFactory));
            if (log.isDebugEnabled()) {
                for (String pkg : packages) {
                    log.debug("Using auto-configuration base package '{}'", pkg);
                }
            }
        }
        catch (IllegalStateException ex) {
            log.debug("Could not determine auto-configuration package, automatic tenant scanning disabled.", ex);
        }

        if(packages.isEmpty()){
            for(String each: ((DefaultListableBeanFactory)beanFactory).getBeanDefinitionNames()) {
                try {
                    BeanDefinition beanDefinition = ((DefaultListableBeanFactory) beanFactory).getBeanDefinition(each);
                    String beanName = beanDefinition.getBeanClassName();
                    Class<?> cls = resourceLoader.getClassLoader().loadClass(beanName);
                    if (cls.getAnnotation(SpringBootApplication.class) != null) {
                        ComponentScan componentScan = cls.getAnnotation(ComponentScan.class);
                        if (componentScan != null) {
                            packages.addAll(Arrays.asList(componentScan.value()));
                        }
                        ComponentScans componentScans = cls.getAnnotation(ComponentScans.class);
                        if (componentScans != null) {
                            for (ComponentScan c : componentScans.value()) {
                                packages.addAll(Arrays.asList(c.value()));
                            }
                        }
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug("class not found.", e);
                }
            }
        }
        scanner.setAnnotationClass(ProxyServiceInterface.class);
        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(packages));
    }
}
