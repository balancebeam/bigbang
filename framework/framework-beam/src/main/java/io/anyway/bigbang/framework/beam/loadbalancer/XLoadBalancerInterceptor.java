package io.anyway.bigbang.framework.beam.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@Slf4j
public class XLoadBalancerInterceptor extends LoadBalancerInterceptor {

    final private LoadBalancerInterceptor delegate;

    public XLoadBalancerInterceptor(LoadBalancerInterceptor delegate){
        super(null, null);
        this.delegate= delegate;
        log.info("delegate LoadBalancerInterceptor: {}",delegate);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        try {
            return delegate.intercept(request, body, execution);
        }catch(IllegalStateException e){
            String errMsg= e.getMessage();
            if(errMsg!=null && errMsg.startsWith("No instances available")){
                log.info("Use default URI: {} to invoke api",request.getURI());
                return execution.execute(request,body);
            }
            throw e;
        }
    }
}
