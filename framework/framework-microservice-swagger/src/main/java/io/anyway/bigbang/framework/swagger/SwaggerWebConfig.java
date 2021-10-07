package io.anyway.bigbang.framework.swagger;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerWebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //设置允许跨域的路径
        registry.addMapping("/**") //所有的当前站点的请求地址，都支持跨域访问
                //设置允许跨域请求的域名,二选一allowedOrigins或allowedOrigins
                .allowedOrigins("*") // 所有的外部域都可跨域访问
                //放行哪些原始域(请求方式)
                .allowedMethods("*")
                //设置允许的请求头
                .allowedHeaders("*")
                //是否允许证书(是否支持跨域用户凭证),不再默认开启
                .allowCredentials(true)
                //跨域允许时间
                .maxAge(3600);
    }
    /**
     * 解决swagger被拦截的问题
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("swagger-ui.html")
//            registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}
