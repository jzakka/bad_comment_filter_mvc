package com.example.bad_comment_filter_mvc.service;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import com.example.bad_comment_filter_mvc.exception.CommentIdInvalidException;
import com.example.bad_comment_filter_mvc.exception.ParallelProcessingException;
import com.example.bad_comment_filter_mvc.repository.CommentRepository;
import com.example.bad_comment_filter_mvc.restclient.ModelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static final int BATCH_SIZE = 4;
    private final CommentRepository commentRepository;
    private final ModelClient modelClient;

    public List<CommentResponse> getPredictionResultsByBatch(List<CommentRequest> commentRequests){
        idCheck(commentRequests);

        List<CompletableFuture<List<CommentResponse>>> futures = IntStream.range(0, (commentRequests.size() + BATCH_SIZE - 1) / BATCH_SIZE)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> getPredictionResults(i, commentRequests.subList(i * BATCH_SIZE, Math.min((i + 1) * BATCH_SIZE, commentRequests.size())))))
                .toList();

        List<CommentResponse> predictionResults = new ArrayList<>();
        for (CompletableFuture<List<CommentResponse>> future : futures) {
            try {
                predictionResults.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new ParallelProcessingException(e);
            }
        }
        return predictionResults;
    }

    public List<CommentResponse> getPredictionResults(int batchGroupNum, List<CommentRequest> commentRequests) {
        final int OFFSET = batchGroupNum * BATCH_SIZE;
        List<CommentResponse> cachedResults = commentRequests.stream()
                .map(commentRepository::getCachedResults)
                .toList();

        List<CommentResponse> notCached = cachedResults.stream()
                .filter(CommentResponse::isEmpty)
                .toList();

        List<String> modelPacket = notCached.stream()
                .map(emptyResult -> commentRequests.get(emptyResult.id() - OFFSET).text())
                .toList();

        List<List<PredictionResult>> predictionResults;
        if (modelPacket.isEmpty()) {
            predictionResults = Collections.emptyList();
        } else {
            predictionResults = modelClient.send(modelPacket);
        }

        commentRepository.save(modelPacket, predictionResults);

        return mergeResults(OFFSET, cachedResults, predictionResults);
    }

    private void idCheck(List<CommentRequest> commentRequests) {
        int size = commentRequests.size();
        for (int i = 0; i < size; i++) {
            if (commentRequests.get(i).id() != i) {
                throw new CommentIdInvalidException();
            }
        }
    }

    private List<CommentResponse> mergeResults(int offset, List<CommentResponse> cachedResults, List<List<PredictionResult>> predictionResults) {
        List<CommentResponse> mergedResult = new ArrayList<>(cachedResults);

        int predictResultsIdx = 0;

        for (int i = 0; i < mergedResult.size(); i++) {
            if (mergedResult.get(i).labelPrediction() == null) {
                List<PredictionResult> labelPrediction = predictionResults.get(predictResultsIdx++);
                CommentResponse commentResponse = new CommentResponse(offset+i, labelPrediction);
                mergedResult.set(i, commentResponse);
            }
        }

        return mergedResult;
    }
}
