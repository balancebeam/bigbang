package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.service.RequestPathWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class RequestPathWhiteListServiceImpl implements RequestPathWhiteListService, ApplicationListener<RequestPathWhiteListServiceImpl.WhiteListEvent> {

    private volatile List<String> whiteList= Collections.emptyList();

    private AntPathMatcher antPathMatcher= new AntPathMatcher();

    public void setWhiteList(List<String> whiteList){
        this.whiteList= whiteList;
    }

    @Override
    public boolean match(String path) {
        return whiteList.stream().anyMatch(pattern->antPathMatcher.match(pattern,path));
    }

    @Override
    public void onApplicationEvent(WhiteListEvent event) {
        String content= (String)event.getSource();
        if(StringUtils.isEmpty(content)){
            setWhiteList(Collections.emptyList());
        }
        else {
            String[] slt = content.split("\n");
            setWhiteList(Arrays.asList(slt));
        }
        log.info("WhiteList: {}",whiteList);
    }

    public static class WhiteListEvent extends ApplicationEvent {

        /**
         * Create a new {@code ApplicationEvent}.
         *
         * @param source the object on which the event initially occurred or with
         *               which the event is associated (never {@code null})
         */
        public WhiteListEvent(Object source) {
            super(source);
        }
    }
}
