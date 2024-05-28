package com.example.bad_comment_filter_mvc.repository;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("benchmark")
public class MockCommentRepository implements CommentRepository{
    @Override
    public CommentResponse getCachedResults(CommentRequest commentRequest) {
        return CommentResponse.emptyResponse(commentRequest.id());
    }

    @Override
    public void save(List<String> modelPacket, List<List<PredictionResult>> predictionResults) {
    }
}
