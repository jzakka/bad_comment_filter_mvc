package com.example.bad_comment_filter_mvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BadCommentFilterMvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadCommentFilterMvcApplication.class, args);
    }

}
