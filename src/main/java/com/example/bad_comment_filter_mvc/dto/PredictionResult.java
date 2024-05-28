package com.example.bad_comment_filter_mvc.dto;

import com.example.bad_comment_filter_mvc.serdes.PlainDoubleSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public record PredictionResult(String label,
                               @JsonSerialize(using = PlainDoubleSerializer.class)
                               double score) {
}
