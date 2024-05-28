package com.example.bad_comment_filter_mvc.repository;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;

import java.util.List;

public interface CommentRepository {

    CommentResponse getCachedResults(CommentRequest commentRequest);

    void save(List<String> modelPacket, List<List<PredictionResult>> predictionResults) ;
}
