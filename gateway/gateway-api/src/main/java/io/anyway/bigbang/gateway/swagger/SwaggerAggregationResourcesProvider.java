package io.anyway.bigbang.gateway.swagger;

import io.anyway.bigbang.gateway.service.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 2021-10-16 21:35
 */
@Slf4j
@Primary
public class SwaggerAggregationResourcesProvider implements SwaggerResourcesProvider {

    /**
     * Swagger2默认的url后缀
     */

    public static final String SWAGGER2URL = "/v2/api-docs";
    /**
     * Swagger自定义前缀
     */
    public static final String SWAGGER2URLPREFIX = "swagger-proxy/";

    @Resource
    private DynamicRouteService dynamicRouteService;

    /**
     * Gets a result.
     *
     * @return a result
     */

    @Override
    public List<SwaggerResource> get() {
        List<RouteDefinition> list= dynamicRouteService.getRouteRouteDefinitionList();
        List<SwaggerResource> resources = new ArrayList<>();
        list.stream().forEach(routeDefinition -> {
            Map<String, Object> metadata= routeDefinition.getMetadata();
            if(metadata==null || !"true".equals(String.valueOf(metadata.get("NO_SWAGGER")))){
                resources.add(swaggerResource(routeDefinition.getId(), SWAGGER2URLPREFIX + routeDefinition.getId() + SWAGGER2URL));
            }
        });
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }


}
