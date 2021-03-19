package io.anyway.bigbang.example.api;

import io.anyway.bigbang.example.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("example-provider")
public interface UserClient {

    @GetMapping("/internal/user")
    User getInternalUser(@RequestParam("name") String name);

    @GetMapping("/internal/exception")
    User getInternalExceptionExample();

}
