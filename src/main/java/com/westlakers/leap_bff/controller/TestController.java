package com.westlakers.leap_bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TestController {

    @GetMapping("/public")
    public String publicEndpoint() {
        return "public endpoint";
    }

    @GetMapping("private")
    public String privateEndpoint() {
        return "private endpoint";
    }
}
 
