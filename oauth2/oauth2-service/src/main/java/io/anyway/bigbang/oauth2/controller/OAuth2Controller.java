package io.anyway.bigbang.oauth2.controller;

import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Controller
@RequestMapping("/api")
public class OAuth2Controller {

    @Resource
    private PasswordEncoder passwordEncoder;

    @ResponseBody
    @GetMapping("/password/encode/{rawStr}")
    public ApiResponseEntity<String> encode(@PathVariable String rawStr){
        return ApiResponseEntity.ok(passwordEncoder.encode(rawStr));
    }

    @PostConstruct
    public void init(){
//        String s= passwordEncoder.encode("123");
//        System.out.println(s);
    }

}
