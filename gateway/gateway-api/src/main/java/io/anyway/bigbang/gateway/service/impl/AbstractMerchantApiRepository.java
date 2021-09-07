package io.anyway.bigbang.gateway.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

@Slf4j
public abstract class AbstractMerchantApiRepository<T> {

    private Random random= new Random();

    @Resource
    private DiscoveryClient discoveryClient;

    @Resource
    private RestTemplate restTemplate;

    protected T execute(String path,Class<T> type){
        List<ServiceInstance> instanceList = discoveryClient.getInstances("merchant-openapi-repository");
        if (CollectionUtils.isEmpty(instanceList)) {
            throw new RuntimeException("merchant openapi repository was not existing.");
        }
        int index= random.nextInt(instanceList.size());
        ServiceInstance inst= instanceList.get(index);
        String url= "http://"+inst.getHost()+":"+inst.getHost() + path;
        log.info("merchant api url: {}",url);
        T result = restTemplate.getForObject(url,type);
        log.info("merchant api response: {}",result);
        return result;
    }
}
