package com.example.bad_comment_filter_mvc.restclient;

import com.example.bad_comment_filter_mvc.dto.PredictionResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "NLPModelOpenFeign", url = "${model.url}/api/classify")
public interface ModelClient {
    @PostMapping
    List<List<PredictionResult>> send(@RequestBody  List<String> packet);
}
