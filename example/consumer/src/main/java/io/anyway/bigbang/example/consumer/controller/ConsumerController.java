package io.anyway.bigbang.example.consumer.controller;

import io.anyway.bigbang.example.api.UserClient;
import io.anyway.bigbang.example.model.User;
import io.anyway.bigbang.framework.model.api.ApiResponse;
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
    public ApiResponse<User> getMicroServiceUser(@RequestParam String name){
        User user= userClient.getInternalUser(name);
        return ApiResponse.ok(user);
    }

    @ResponseBody
    @GetMapping("/exception")
    public ApiResponse<User> getMicroServiceException(){
        User user= userClient.getInternalExceptionExample();
        return ApiResponse.ok(user);
    }
}
