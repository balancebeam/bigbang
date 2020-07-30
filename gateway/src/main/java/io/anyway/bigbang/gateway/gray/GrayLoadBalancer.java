package io.anyway.bigbang.gateway.gray;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        String unit= headers.getFirst(GRAY_UNIT_NAME);
        if(!StringUtils.isEmpty(unit)){
            String value= headers.getFirst(GRAY_INDICATOR_NAME);
            String indicator= StringUtils.isEmpty(value)? GRAY_DEFAULT_INDICATOR: value;
            List<ServiceInstance> availableInstances= instances.stream().filter(each-> {
                String v= each.getMetadata().get(indicator);
                if(StringUtils.isEmpty(v)){
                    v= GRAY_DEFAULT_UNIT;
                }
                return unit.equals(v);
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