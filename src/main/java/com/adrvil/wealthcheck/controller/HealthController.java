package com.adrvil.wealthcheck.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String health() {
        return "Application is up and running";
    }

    @GetMapping("/test")
    public String test() {
        return "You are authenticated!";
    }
}
