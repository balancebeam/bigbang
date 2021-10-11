package io.anyway.bigbang.framework.swagger.config;

import com.google.common.base.Predicates;
import io.anyway.bigbang.framework.swagger.SwaggerWebConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.AbstractPathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
@EnableSwagger2
@ImportAutoConfiguration(SwaggerWebConfig.class)
@ConditionalOnClass(HttpServletRequest.class)
public class Swagger2Configure {

    @Value("${spring.swagger.appPath:}")
    private String appPath;

    @Value("${spring.swagger.title:Service Api List}")
    private String title;

    @Value("${spring.swagger.description:Api}")
    private String description;

    @Value("${spring.swagger.version:1.0.0}")
    private String version;

    @Value("${spring.swagger.token:access_token}")
    private String token;

    @Bean
    @ConditionalOnMissingBean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .globalOperationParameters(setHeaderToken())
                .pathProvider(new AbstractPathProvider() {
                    @Override
                    protected String applicationPath() {
                        return appPath;
                    }
                    @Override
                    protected String getDocumentationPath() {
                        return "/";
                    }
                })
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RequestMapping.class))
                .paths(PathSelectors.any())
                .paths(Predicates.not(PathSelectors.regex("/error.*")))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .contact(new Contact("development","bigbang.anyway.io","development@bigbang.anyway.io"))
                .version(version)
                .license("The Apache License")
                .licenseUrl("http://license.bigbang.anyway.io")
                .build();
    }

    private List<Parameter> setHeaderToken() {
        ParameterBuilder tokenPar = new ParameterBuilder();
        tokenPar.name(token).description("user auth token").modelRef(new ModelRef("string")).parameterType("header").required(false).build();
        List<Parameter> pars = new ArrayList<>();
        pars.add(tokenPar.build());
        return pars;
    }

}
