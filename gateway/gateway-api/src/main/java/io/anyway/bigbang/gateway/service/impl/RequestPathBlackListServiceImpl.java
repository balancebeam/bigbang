package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.service.RequestPathBlackListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class RequestPathBlackListServiceImpl implements RequestPathBlackListService, ApplicationListener<RequestPathBlackListServiceImpl.BlackListEvent> {

    private volatile List<String> blackList= Collections.emptyList();

    private AntPathMatcher antPathMatcher= new AntPathMatcher();

    @Override
    public boolean match(String path) {
        return blackList.stream().anyMatch(pattern->antPathMatcher.match(pattern,path));
    }

    public void setBlackList(List<String> blackList){
        this.blackList= blackList;
    }

    @Override
    public void onApplicationEvent(BlackListEvent event) {
        String content= (String)event.getSource();
        if(StringUtils.isEmpty(content)){
            setBlackList(Collections.emptyList());
        }
        else {
            String[] slt = content.split("\n");
            setBlackList(Arrays.asList(slt));
        }
        log.info("WhiteList: {}",blackList);
    }

    public static class BlackListEvent extends ApplicationEvent{

        /**
         * Create a new {@code ApplicationEvent}.
         *
         * @param source the object on which the event initially occurred or with
         *               which the event is associated (never {@code null})
         */
        public BlackListEvent(Object source) {
            super(source);
        }
    }
}
