package io.anyway.bigbang.test.plugin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/hello")
public class PluginController {

    @RequestMapping(value = "/world", method = RequestMethod.GET)
    public String helloworld() {
        return "helloworld";
    }
}
