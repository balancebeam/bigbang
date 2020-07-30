package io.anyway.bigbang.framework.tenant.proxy;

import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.http.HttpProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.PathMatcher;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.handler.ConversionServiceExposingInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.ServletRegistration;
import java.util.List;
import java.util.Map;

@ConditionalOnClass(Tomcat.class)
public class PluginWebMvcConfig {

    @ConditionalOnClass({ServletRegistration.class})
    @Bean(name = {"dispatcherServlet"})
    public PluginDispatcherServlet dispatcherServlet(HttpProperties httpProperties, WebMvcProperties webMvcProperties) {
        PluginDispatcherServlet dispatcherServlet = new PluginDispatcherServlet();
        dispatcherServlet.setDispatchOptionsRequest(webMvcProperties.isDispatchOptionsRequest());
        dispatcherServlet.setDispatchTraceRequest(webMvcProperties.isDispatchTraceRequest());
        dispatcherServlet.setThrowExceptionIfNoHandlerFound(webMvcProperties.isThrowExceptionIfNoHandlerFound());
        dispatcherServlet.setPublishEvents(webMvcProperties.isPublishRequestHandledEvents());
        dispatcherServlet.setEnableLoggingRequestDetails(httpProperties.isLogRequestDetails());
        return dispatcherServlet;
    }

    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping(
            @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
            @Qualifier("mvcConversionService") FormattingConversionService conversionService,
            @Qualifier("mvcResourceUrlProvider") ResourceUrlProvider resourceUrlProvider) {

        RequestMappingHandlerMapping mapping = createRequestMappingHandlerMapping();
        mapping.setOrder(0);
        mapping.setInterceptors(getInterceptors(conversionService, resourceUrlProvider));
        mapping.setContentNegotiationManager(contentNegotiationManager);
        mapping.setCorsConfigurations(getCorsConfigurations());

        PathMatchConfigurer configurer = new PathMatchConfigurer();
        if (configurer.isUseSuffixPatternMatch() != null) {
            mapping.setUseSuffixPatternMatch(configurer.isUseSuffixPatternMatch());
        }
        if (configurer.isUseRegisteredSuffixPatternMatch() != null) {
            mapping.setUseRegisteredSuffixPatternMatch(configurer.isUseRegisteredSuffixPatternMatch());
        }
        if (configurer.isUseTrailingSlashMatch() != null) {
            mapping.setUseTrailingSlashMatch(configurer.isUseTrailingSlashMatch());
        }
        UrlPathHelper pathHelper = configurer.getUrlPathHelper();
        if (pathHelper != null) {
            mapping.setUrlPathHelper(pathHelper);
        }
        PathMatcher pathMatcher = configurer.getPathMatcher();
        if (pathMatcher != null) {
            mapping.setPathMatcher(pathMatcher);
        }

        return mapping;
    }

    /**
     * Protected method for plugging in a custom subclass of
     * {@link RequestMappingHandlerMapping}.
     * @since 4.0
     */
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping(){
            @Override
            protected String[] getCandidateBeanNames() {
                return obtainApplicationContext().getBeanDefinitionNames();
            }
        };
    }

    public static class XInterceptorRegistry extends InterceptorRegistry{
        public List<Object> getInterceptors2(){
            return super.getInterceptors();
        }
    }

    protected final Object[] getInterceptors(
            FormattingConversionService mvcConversionService,
        ResourceUrlProvider mvcResourceUrlProvider) {
        XInterceptorRegistry registry = new XInterceptorRegistry();
        registry.addInterceptor(new ConversionServiceExposingInterceptor(mvcConversionService));
        registry.addInterceptor(new ResourceUrlProviderExposingInterceptor(mvcResourceUrlProvider));
        return registry.getInterceptors2().toArray();
    }

    public static class XCorsRegistry extends CorsRegistry{
        public Map<String, CorsConfiguration> getCorsConfigurations2(){
            return super.getCorsConfigurations();
        }
    }

    protected final Map<String, CorsConfiguration> getCorsConfigurations() {
        XCorsRegistry registry = new XCorsRegistry();
        return registry.getCorsConfigurations2();
    }


}
