package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private String serviceId;
    private final AtomicInteger position;
    private GrayRibbonRule grayRibbonRule;

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, GrayRibbonRule grayRibbonRule, String serviceId) {
        this.grayRibbonRule= grayRibbonRule;
        this.serviceId = serviceId;
        this.position= new AtomicInteger(new Random().nextInt(1000));
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        Optional<GrayContext> grayContext = (Optional<GrayContext>) request.getContext();
        if (this.serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
            return ((Flux)supplier.get()).next().map(list->getInstanceResponse((List<ServiceInstance>)list,grayContext));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances,Optional<GrayContext> grayContext) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        if(grayContext.isPresent()){
            return grayRibbonRule.choose(serviceId,instances,grayContext.get());
        }
        int pos = Math.abs(this.position.incrementAndGet());
        ServiceInstance instance = instances.get(pos % instances.size());
        return new DefaultResponse(instance);
    }

}