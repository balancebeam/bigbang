package io.anyway.bigbang.example.consumer.controller;

import io.anyway.bigbang.example.api.UserClient;
import io.anyway.bigbang.example.model.User;
import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/api")
public class ConsumerController {

    @Resource
    private UserClient userClient;

    @ResponseBody
    @GetMapping("/user")
    public ApiResponseEntity<User> getMicroServiceUser(@RequestParam String name){
        User user= userClient.getInternalUser(name);
        return ApiResponseEntity.ok(user);
    }

    @ResponseBody
    @GetMapping("/exception")
    public ApiResponseEntity<User> getMicroServiceException(){
        User user= userClient.getInternalExceptionExample();
        return ApiResponseEntity.ok(user);
    }
}
