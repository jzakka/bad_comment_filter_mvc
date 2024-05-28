package com.example.bad_comment_filter_mvc.controller;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://gall.dcinside.com", "https://n.news.naver.com"}, originPatterns = {"https://*.youtube.com"})
public class CommentController {
    private final CommentService commentService;

    @PostMapping(value = "/api/blind")
    public List<CommentResponse> blindBadComments(@RequestBody List<CommentRequest> commentRequests) {
        return commentService.getPredictionResultsByBatch(commentRequests);
    }
}
