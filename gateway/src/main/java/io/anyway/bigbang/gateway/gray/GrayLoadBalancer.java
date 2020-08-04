package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.discovery.GrayRouteContext;
import io.anyway.bigbang.framework.discovery.GrayRouteContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.ATTRIBUTE_CLUSTER_NAME;
import static io.anyway.bigbang.framework.discovery.GrayRouteContext.ATTRIBUTE_GROUP;

@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    private final AtomicInteger position;

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceId = serviceId;
        this.position= new AtomicInteger(new Random().nextInt(1000));
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        HttpHeaders headers = (HttpHeaders) request.getContext();
        if (this.serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
            return ((Flux)supplier.get()).next().map(list->getInstanceResponse((List<ServiceInstance>)list,headers));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances,HttpHeaders headers) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());
        Optional<GrayRouteContext> crayRouteContext= GrayRouteContextHolder.getGrayRouteContext();
        if(crayRouteContext.isPresent()){
            GrayRouteContext ctx= crayRouteContext.get();
            List<ServiceInstance> availableInstances= instances.stream().filter(each-> {
                String group= each.getMetadata().get(ATTRIBUTE_GROUP);
                String clusterName= each.getMetadata().get(ATTRIBUTE_CLUSTER_NAME);
                return group.equals(ctx.getGroup()) && clusterName.equals(ctx.getClusterName());
            }).collect(Collectors.toList());
            if(!availableInstances.isEmpty()){
                ServiceInstance instance= availableInstances.get(pos % availableInstances.size());
                return new DefaultResponse(instance);
            }
        }

        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }

}