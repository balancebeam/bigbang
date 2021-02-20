package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.service.WhiteListService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.Collections;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "spring.cloud.gateway")
public class WhiteListServiceImpl implements WhiteListService , InitializingBean {

    private List<String> whiteList= Collections.emptyList();

    private AntPathMatcher antPathMatcher= new AntPathMatcher();

    public void setWhiteList(List<String> whiteList){
        this.whiteList= whiteList;
    }

    @Override
    public boolean match(String path) {
        return whiteList.stream().anyMatch(pattern->antPathMatcher.match(pattern,path));
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
