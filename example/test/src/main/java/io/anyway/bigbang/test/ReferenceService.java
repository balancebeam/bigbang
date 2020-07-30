package io.anyway.bigbang.test;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Setter
@Getter
@ToString
public class ReferenceService {

    HelloService service;

    @Resource
    private HelloProperties properties;

    public ReferenceService(HelloService service){
        this.service= service;
    }

    @PostConstruct
    public void init(){
        System.out.println(properties);
    }


}
