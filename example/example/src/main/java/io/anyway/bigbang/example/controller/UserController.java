package io.anyway.bigbang.example.controller;

import io.anyway.bigbang.example.entity.User;
import io.anyway.bigbang.example.service.UserService;
import io.anyway.bigbang.framework.core.rest.RestBody;
import io.anyway.bigbang.framework.core.rest.RestHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(){
        return "hello";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public RestHeader addUser(@RequestBody User user){
        userService.addUser(user);
        RestHeader header= new RestHeader();
        header.setMsg("Add Success");
        return header;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public RestBody<List<User>> getUsers(){
        RestBody<List<User>> header= new RestBody<>();
        header.setMsg("Query All Users");
        header.setData(userService.getUsers());
        return header;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public RestBody<User> getUser(@PathVariable Long id){
        RestBody<User> header= new RestBody<>();
        header.setMsg("Query Single User");
        header.setData(userService.getUser(id));
        return header;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public RestHeader deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        RestHeader header= new RestHeader();
        header.setMsg("Delete Success");
        return header;
    }

    @RequestMapping(value = "/sharding/", method = RequestMethod.POST)
    public RestHeader addShardingUser(@RequestBody User user){
        userService.addShardingUser(user);
        RestHeader header= new RestHeader();
        header.setMsg("Add Success");
        return header;
    }

    @RequestMapping(value = "/sharding/", method = RequestMethod.GET)
    public RestBody<List<User>> getShardingUsers(){
        RestBody<List<User>> header= new RestBody<>();
        header.setMsg("Query All Users");
        header.setData(userService.getShardingUsers());
        return header;
    }

    @RequestMapping(value = "/sharding/{id}", method = RequestMethod.GET)
    public RestBody<User> getShardingUser(@PathVariable Long id){
        RestBody<User> header= new RestBody<>();
        header.setMsg("Query Single User");
        header.setData(userService.getShardingUser(id));
        return header;
    }

    @RequestMapping(value = "/sharding/{id}", method = RequestMethod.DELETE)
    public RestHeader deleteShardingUser(@PathVariable Long id){
        userService.deleteShardingUser(id);
        RestHeader header= new RestHeader();
        header.setMsg("Delete Success");
        return header;
    }

}
