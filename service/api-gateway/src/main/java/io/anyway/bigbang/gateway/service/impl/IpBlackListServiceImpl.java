package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.property.GatewayProperties;
import io.anyway.bigbang.gateway.service.IpBlackListService;
import io.anyway.bigbang.gateway.service.ResourceStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class IpBlackListServiceImpl implements IpBlackListService , ResourceStrategyService {

    @Resource
    private GatewayProperties gatewayProperties;

    private List<IpAddressMatcher> blackListMatchers= new CopyOnWriteArrayList<>();

    private ConcurrentHashMap<String, IpAddressMatcher> mapping= new ConcurrentHashMap<>();

    @Override
    public boolean isBlackIpAddress(String address) {
        return blackListMatchers.stream().anyMatch(p->p.matches(address));
    }

    @Override
    public void addResourceStrategy(String id, String ipPattern) {
        IpAddressMatcher matcher= new IpAddressMatcher(ipPattern);
        blackListMatchers.add(matcher);
        mapping.put(id,matcher);
        log.info("Add IpBlackListMatcher: {}",matcher);
    }

    @Override
    public void updateResourceStrategy(String id, String ipPattern) {
        IpAddressMatcher matcher= new IpAddressMatcher(ipPattern);
        blackListMatchers.remove(mapping.get(id));
        blackListMatchers.add(matcher);
        mapping.put(id,matcher);
        log.info("Update IpBlackListMatcher: {}",matcher);
    }

    @Override
    public void removeResourceStrategy(String id) {
        if(mapping.containsKey(id)) {
            IpAddressMatcher matcher= mapping.remove(id);
            blackListMatchers.remove(matcher);
            log.info("Remove IpBlackListMatcher: {}",matcher);
        }

    }

    @PostConstruct
    public void init(){
        gatewayProperties.getIpBlackList().forEach(ip-> blackListMatchers.add(new IpAddressMatcher(ip)));
        gatewayProperties.setIpBlackList( null);
        log.info("Ip black list: {}",blackListMatchers);
    }
}
