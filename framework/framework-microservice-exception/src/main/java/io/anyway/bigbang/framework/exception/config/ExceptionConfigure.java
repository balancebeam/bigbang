package io.anyway.bigbang.framework.exception.config;

import io.anyway.bigbang.framework.exception.FeignClientErrorDecoder;
import io.anyway.bigbang.framework.exception.GlobalExceptionHandler;
import io.anyway.bigbang.framework.exception.RestTemplateResponseErrorHandler;
import feign.codec.ErrorDecoder;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
@ImportAutoConfiguration(RestTemplateErrorConfigure.class)
@ConditionalOnClass({ErrorDecoder.class,HttpServletRequest.class})
public class ExceptionConfigure {

    @Bean
    public FeignClientErrorDecoder createFeignClientErrorDecoder(){
        return new FeignClientErrorDecoder();
    }

    @Bean
    public GlobalExceptionHandler createGlobalExceptionHandler(){
        return new GlobalExceptionHandler();
    }

    @Bean
    public RestTemplateResponseErrorHandler createRestTemplateResponseErrorHandler(){
        return new RestTemplateResponseErrorHandler();
    }

}
