package io.anyway.bigbang.example.controller;

import io.anyway.bigbang.example.model.User;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.framework.kernel.api.APIResponse;
import io.anyway.bigbang.framework.kernel.exception.ApiException;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public APIResponse<User> getUser(@PathVariable String name){
        Optional<User> user= userService.getUser(name);
        User u= user.orElseThrow(() -> new NoSuchElementException("No such User with name " + name));
        return APIResponse.ok(u);
    }

    @ResponseBody
    @PostMapping("/user")
    @ApiOperation("save user")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "user object"),
    })
    public APIResponse<String> createUser(@RequestBody User person){
        return APIResponse.ok("add successfully");
    }

    @ResponseBody
    @GetMapping("/exception")
    public APIResponse<String> exception(){
        ApiException ex= new ApiException(4001,new Object[]{"1","2"});
        ex.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        throw ex;
    }

}
