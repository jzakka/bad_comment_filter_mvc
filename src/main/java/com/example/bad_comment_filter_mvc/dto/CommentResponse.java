package com.example.bad_comment_filter_mvc.dto;

import java.util.List;
import java.util.Map;

public record CommentResponse(int id, List<PredictionResult> labelPrediction) {
    public static CommentResponse from(int id, Map<String, String> predictionMap) {
        List<PredictionResult> predictionResults = predictionMap.entrySet().stream()
                .map(e -> new PredictionResult(e.getKey(), Double.parseDouble(e.getValue())))
                .toList();

        return new CommentResponse(id, predictionResults);
    }
}
