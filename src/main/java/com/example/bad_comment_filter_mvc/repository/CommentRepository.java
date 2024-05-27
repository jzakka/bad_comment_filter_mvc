package com.example.bad_comment_filter_mvc.repository;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import com.example.bad_comment_filter_mvc.util.CacheKey;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CommentRepository {

    private final HashOperations<String, String, String> opsForHash;

    public CommentRepository(StringRedisTemplate redisTemplate) {
        this.opsForHash = redisTemplate.opsForHash();
    }

    public CommentResponse getCachedResults(CommentRequest commentRequest) {
        String cacheKey = CacheKey.getKeyFromText(commentRequest.text());

        Map<String, String> cachedPrediction = opsForHash.entries(cacheKey);
        if (cachedPrediction.isEmpty()) {
            return new CommentResponse(commentRequest.id(), null);
        }

        return CommentResponse.from(commentRequest.id(), cachedPrediction);
    }

    public void save(List<String> modelPacket, List<List<PredictionResult>> predictionResults) {
        int len = modelPacket.size();

        for (int i = 0; i < len; i++) {
            String text = modelPacket.get(i);
            List<PredictionResult> predictionResult = predictionResults.get(i);
            String key = CacheKey.getKeyFromText(text);

            Map<String, String> stringfiedResults = stringfy(predictionResult);
            opsForHash.putAll(key, stringfiedResults);
        }
    }

    private Map<String, String> stringfy(List<PredictionResult> predictionResults) {
        return predictionResults.stream()
                .collect(Collectors.toMap(prediction -> prediction.label(), prediction -> String.valueOf(prediction.score())));

    }
}
