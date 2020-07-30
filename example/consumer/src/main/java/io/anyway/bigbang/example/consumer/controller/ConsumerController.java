package io.anyway.bigbang.example.consumer.controller;

import io.anyway.bigbang.framework.kernel.api.APIResponse;
import io.anyway.bigbang.example.api.UserClient;
import io.anyway.bigbang.example.model.User;
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
    public APIResponse<User> getMicroServiceUser(@RequestParam String name){
        User user= userClient.getInternalUser(name);
        return APIResponse.ok(user);
    }

    @ResponseBody
    @GetMapping("/exception")
    public APIResponse<User> getMicroServiceException(){
        User user= userClient.getInternalExceptionExample();
        return APIResponse.ok(user);
    }
}
