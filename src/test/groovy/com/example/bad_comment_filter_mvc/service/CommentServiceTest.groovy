package com.example.bad_comment_filter_mvc.service

import com.example.bad_comment_filter_mvc.dto.CommentRequest
import com.example.bad_comment_filter_mvc.dto.CommentResponse
import com.example.bad_comment_filter_mvc.dto.PredictionResult
import com.example.bad_comment_filter_mvc.exception.CommentIdInvalidException
import com.example.bad_comment_filter_mvc.repository.CommentRepository
import com.example.bad_comment_filter_mvc.restclient.ModelClient
import spock.lang.Specification

class CommentServiceTest extends Specification {
    def commentRepository = Mock(CommentRepository)
    def modelClient = Mock(ModelClient)
    def commentService = new CommentService(commentRepository, modelClient)

    def "모든 댓글이 캐싱돼있음"() {
        given:
        def commentRequests = [
                new CommentRequest(0, "텍스트1"),
                new CommentRequest(1, "텍스트2"),
                new CommentRequest(2, "텍스트3"),
                new CommentRequest(3, "텍스트4"),
        ]
        commentRepository.getCachedResults(commentRequests[0]) >> new CommentResponse(0, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[1]) >> new CommentResponse(1, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[2]) >> new CommentResponse(2, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[3]) >> new CommentResponse(3, [new PredictionResult("", 0)])
        when:
        commentService.getPredictionResults(0,commentRequests)
        then:
        0 * modelClient.send(_)
    }

    def "앞의 반은 캐싱O, 뒤의 반은 캐싱X"() {
        given:
        def commentRequests = [
                new CommentRequest(0, "텍스트1"),
                new CommentRequest(1, "텍스트2"),
                new CommentRequest(2, "텍스트3"),
                new CommentRequest(3, "텍스트4"),
        ]
        commentRepository.getCachedResults(commentRequests[0]) >> new CommentResponse(0, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[1]) >> new CommentResponse(1, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[2]) >> new CommentResponse(2, null)
        commentRepository.getCachedResults(commentRequests[3]) >> new CommentResponse(3, null)
        when:
        def predictionResults = commentService.getPredictionResults(0,commentRequests)
        then:
        1 * modelClient.send(_) >> [[],[]]
        predictionResults[0].id() == 0
        predictionResults[1].id() == 1
        predictionResults[2].id() == 2
        predictionResults[3].id() == 3
    }

    def "prediction 결과는 요청 때 순서를 유지해야 함."() {
        given:
        def commentRequests = [
                new CommentRequest(0, "텍스트1"),
                new CommentRequest(1, "텍스트2"),
                new CommentRequest(2, "텍스트3"),
                new CommentRequest(3, "텍스트4"),
        ]
        commentRepository.getCachedResults(commentRequests[0]) >> new CommentResponse(0, [new PredictionResult("", 0)])
        commentRepository.getCachedResults(commentRequests[1]) >> new CommentResponse(1, null)
        commentRepository.getCachedResults(commentRequests[2]) >> new CommentResponse(2, null)
        commentRepository.getCachedResults(commentRequests[3]) >> new CommentResponse(3, [new PredictionResult("", 0)])
        when:
        def predictionResults = commentService.getPredictionResults(0,commentRequests)
        then:
        1 * modelClient.send(_) >> [[],[]]
        predictionResults[0].id() == 0
        predictionResults[1].id() == 1
        predictionResults[2].id() == 2
        predictionResults[3].id() == 3
    }

    def "id는 인덱스임. 0부터 연속적이여야 함."() {
        given:
        def commentRequests = [
                new CommentRequest(2, "텍스트1"),
                new CommentRequest(4, "텍스트2"),
                new CommentRequest(6, "텍스트3"),
                new CommentRequest(8, "텍스트4"),
        ]
        commentRepository.getCachedResults(_) >> new CommentResponse(0, null)
        when:
        commentService.getPredictionResultsByBatch(commentRequests)
        then:
        def e = thrown(CommentIdInvalidException)
        e.message == CommentIdInvalidException.ERR_MESSAGE
    }

    def "BATCH_SIZE이상의 prediction result의 id가 뒤섞이지 않아야 함"() {
        given:
        List<CommentRequest> requests = new ArrayList()
        for (i in 0..<100) {
            requests.add(new CommentRequest(i, ""))
        }
        for (int i=1;i<100;i+=2) {
            commentRepository.getCachedResults(requests.get(i)) >> new CommentResponse(i, [new PredictionResult("",0)])
        }
        for (int i=0;i<100;i+=2) {
            commentRepository.getCachedResults(requests.get(i)) >> CommentResponse.emptyResponse(i)
            modelClient.send(_) >> [
                    [new PredictionResult("", 0)],
                    [new PredictionResult("", 0)],
                    [new PredictionResult("", 0)],
                    [new PredictionResult("", 0)],
                    [new PredictionResult("", 0)],
            ]
        }
        when:
        def result = commentService.getPredictionResultsByBatch(requests)
        then:
        for (i in 0..<100) {
            result[i].id() == i
        }
    }
}
