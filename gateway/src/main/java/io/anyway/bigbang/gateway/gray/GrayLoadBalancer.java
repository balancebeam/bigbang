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
        Optional<GrayRouteContext> grayRouteContext = (Optional<GrayRouteContext>) request.getContext();
        if (this.serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
            return ((Flux)supplier.get()).next().map(list->getInstanceResponse((List<ServiceInstance>)list,grayRouteContext));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances,Optional<GrayRouteContext> grayRouteContext) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }

        int pos = Math.abs(this.position.incrementAndGet());
        if(grayRouteContext.isPresent()){
            GrayRouteContext ctx= grayRouteContext.get();
            List<ServiceInstance> availableInstances= instances.stream().filter(each-> {
                String cluster= each.getMetadata().get(ATTRIBUTE_CLUSTER_NAME);
                return cluster.equals(ctx.getCluster());
            }).collect(Collectors.toList());
            //lookup the default group and cluster
            if(availableInstances.isEmpty() &&
                    ctx.getDefaultCluster()!= null &&
                    !ctx.getCluster().equals(ctx.getDefaultCluster())){
                availableInstances= instances.stream().filter(each-> {
                    String cluster= each.getMetadata().get(ATTRIBUTE_CLUSTER_NAME);
                    return cluster.equals(ctx.getDefaultCluster());
                }).collect(Collectors.toList());
            }
            if(!availableInstances.isEmpty()){
                ServiceInstance instance= availableInstances.get(pos % availableInstances.size());
                return new DefaultResponse(instance);
            }
            log.warn("cannot find appropriate match candidate server: {}",ctx.toString());
            return new EmptyResponse();
        }
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }

}