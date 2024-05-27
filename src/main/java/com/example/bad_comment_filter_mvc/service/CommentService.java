package com.example.bad_comment_filter_mvc.service;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import com.example.bad_comment_filter_mvc.repository.CommentRepository;
import com.example.bad_comment_filter_mvc.restclient.ModelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final int BATCH_SIZE = 10;
    private final CommentRepository commentRepository;
    private final ModelClient modelClient;


    public List<CommentResponse> getPredictionResults(List<CommentRequest> commentRequests) {
        List<CommentResponse> cachedResults = commentRequests.stream()
                .map(commentRepository::getCachedResults)
                .toList();

        List<CommentResponse> notCached =  cachedResults.stream()
                .filter(result -> Objects.isNull(result.labelPrediction()))
                .toList();

        List<String> modelPacket = notCached.stream()
                .map(emptyResult -> commentRequests.get(emptyResult.id()).text())
                .toList();

        List<List<PredictionResult>> predictionResults = modelClient.send(modelPacket);

        commentRepository.save(modelPacket, predictionResults);

        return mergeResults(cachedResults, predictionResults);
    }

    private List<CommentResponse> mergeResults(List<CommentResponse> cachedResults, List<List<PredictionResult>> predictionResults) {
        List<CommentResponse> mergedResult = new ArrayList<>(cachedResults);

        int predictResultsIdx = 0;

        for (int i = 0; i < mergedResult.size(); i++) {
            if (mergedResult.get(i).labelPrediction() == null) {
                List<PredictionResult> labelPrediction = predictionResults.get(predictResultsIdx++);
                CommentResponse commentResponse = new CommentResponse(i, labelPrediction);
                mergedResult.set(i, commentResponse);
            }
        }

        return mergedResult;
    }
}
