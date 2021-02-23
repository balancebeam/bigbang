package io.anyway.bigbang.example.controller;

import io.anyway.bigbang.example.model.User;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.framework.exception.ApiException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping("/internal")
public class ProviderInternalController {

    @Resource
    private UserService userService;

    @ResponseBody
    @GetMapping("/user")
    public User getInternalUser(@RequestParam String name){
        Optional<User> user= userService.getUser(name);
        return user.orElseThrow(() -> new NoSuchElementException("No such person with name " + name));
    }

    @ResponseBody
    @GetMapping("/exception")
    public User getInternalException(){
        throw new ApiException("4002");
    }

}
