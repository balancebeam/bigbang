package io.anyway.bigbang.example.controller;

import io.anyway.bigbang.example.model.User;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.framework.exception.ApiException;
import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/api")
@Api(value = "/api/user")
public class ProviderExternalController {

    @Resource
    private UserService userService;

    @ResponseBody
    @GetMapping("/user/{name}")
    @ApiOperation("query user by name")
    @ApiImplicitParam(name = "name", value = "user name", defaultValue = "jerry", required = true)
    @ApiResponse(code=0,message="user information")
    public ApiResponseEntity<User> getUser(@PathVariable String name){
        log.info("get user method");
        Optional<User> user= userService.getUser(name);
        User u= user.orElseThrow(() -> new NoSuchElementException("No such User with name " + name));
        return ApiResponseEntity.ok(u);
    }

    @ResponseBody
    @PostMapping("/user")
    @ApiOperation("save user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "user object"),
    })
    public ApiResponseEntity<String> createUser(@RequestBody User person){
        return ApiResponseEntity.ok("add successfully");
    }

    @ResponseBody
    @GetMapping("/exception")
    public ApiResponseEntity<String> exception(){
        throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,4001,new Object[]{"1","2"});
    }

}
