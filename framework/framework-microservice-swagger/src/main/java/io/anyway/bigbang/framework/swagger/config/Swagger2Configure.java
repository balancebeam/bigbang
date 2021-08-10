package io.anyway.bigbang.framework.swagger.config;

import io.anyway.bigbang.framework.swagger.SwaggerWebConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Configuration
@EnableSwagger2
@ImportAutoConfiguration(SwaggerWebConfig.class)
@ConditionalOnClass(HttpServletRequest.class)
public class Swagger2Configure {

//    @Resource
//    private BeanFactory beanFactory;
//
//    @Resource
//    private ResourceLoader resourceLoader;

    @Bean
    public Docket createRestApi() {
//        List<String> packages= AutoConfigurationPackages.get(this.beanFactory);
//        if(packages.isEmpty()){
//            for(String each: ((DefaultListableBeanFactory)beanFactory).getBeanDefinitionNames()) {
//                try {
//                    BeanDefinition beanDefinition = ((DefaultListableBeanFactory) beanFactory).getBeanDefinition(each);
//                    String beanName = beanDefinition.getBeanClassName();
//                    Class<?> cls = resourceLoader.getClassLoader().loadClass(beanName);
//                    if (cls.getAnnotation(SpringBootApplication.class) != null) {
//                        ComponentScan componentScan = cls.getAnnotation(ComponentScan.class);
//                        if (componentScan != null) {
//                            packages.addAll(Arrays.asList(componentScan.value()));
//                        }
//                        ComponentScans componentScans = cls.getAnnotation(ComponentScans.class);
//                        if (componentScans != null) {
//                            for (ComponentScan c : componentScans.value()) {
//                                packages.addAll(Arrays.asList(c.value()));
//                            }
//                        }
//                        break;
//                    }
//                } catch (ClassNotFoundException e) {
//                    log.debug("class not found.", e);
//                }
//            }
//        }
//        if(packages.isEmpty()){
//            throw new RuntimeException("package was empty.");
//        }

        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .apiInfo(apiInfo())
                .select()
//                .apis(RequestHandlerSelectors.basePackage(packages.get(0)))
                .apis(RequestHandlerSelectors.withClassAnnotation(RequestMapping.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("service api list")
                .description("api")
                .contact(new Contact("development","bigbang.anyway.io","development@bigbang.anyway.io"))
                .version("v1.0.0")
                .license("The Apache License")
                .licenseUrl("http://license.bigbang.anyway.io")
                .build();
    }



}
