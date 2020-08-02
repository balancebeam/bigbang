package io.anyway.bigbang.framework.swagger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Resource
    private BeanFactory beanFactory;

    @Resource
    private ResourceLoader resourceLoader;

    @Bean
    public Docket createRestApi() {
        List<String> packages= AutoConfigurationPackages.get(this.beanFactory);
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
        if(packages.isEmpty()){
            throw new RuntimeException("package was empty.");
        }

        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(packages.get(0)))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("service api list")
                .description("api")
                .contact(new Contact("development","api.bigbang.anyway.io","development@bigbang.anyway.io"))
                .version("v1.0.0")
                .license("The Apache License")
                .licenseUrl("http://license.bigbang.anyway.io")
                .build();
    }



}
