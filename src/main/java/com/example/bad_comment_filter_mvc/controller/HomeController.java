package com.example.bad_comment_filter_mvc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/ping")
    public String ping() {
        return "OK!";
    }
}
