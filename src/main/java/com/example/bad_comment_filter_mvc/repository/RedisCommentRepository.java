package com.example.bad_comment_filter_mvc.repository;

import com.example.bad_comment_filter_mvc.dto.CommentRequest;
import com.example.bad_comment_filter_mvc.dto.CommentResponse;
import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import com.example.bad_comment_filter_mvc.util.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Profile("!benchmark")
public class RedisCommentRepository implements CommentRepository{
    private final HashOperations<String, String, String> opsForHash;

    public RedisCommentRepository(StringRedisTemplate redisTemplate) {
        this.opsForHash = redisTemplate.opsForHash();
    }

    @Override
    public CommentResponse getCachedResults(CommentRequest commentRequest) {
        String cacheKey = CacheKey.getKeyFromText(commentRequest.text());

        Map<String, String> cachedPrediction;
        try {
            cachedPrediction = opsForHash.entries(cacheKey);
        } catch (Exception e) {
            log.error("Failed to load prediction results.");
            return CommentResponse.emptyResponse(commentRequest.id());
        }
        if (cachedPrediction.isEmpty()) {
            return CommentResponse.emptyResponse(commentRequest.id());
        }

        return CommentResponse.from(commentRequest.id(), cachedPrediction);
    }

    @Override
    public void save(List<String> modelPacket, List<List<PredictionResult>> predictionResults) {
        int len = modelPacket.size();

        for (int i = 0; i < len; i++) {
            String text = modelPacket.get(i);
            List<PredictionResult> predictionResult = predictionResults.get(i);
            String key = CacheKey.getKeyFromText(text);

            Map<String, String> stringfiedResults = stringfy(predictionResult);
            try {
                opsForHash.putAll(key, stringfiedResults);
            } catch (Exception e) {
                log.error("Failed to save prediction results.");
            }
        }
    }

    private Map<String, String> stringfy(List<PredictionResult> predictionResults) {
        return predictionResults.stream()
                .collect(Collectors.toMap(prediction -> prediction.label(), prediction -> String.valueOf(prediction.score())));

    }
}
