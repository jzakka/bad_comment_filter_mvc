package com.example.bad_comment_filter_mvc.exception;

public class ParallelProcessingException extends RuntimeException {
    public ParallelProcessingException(Throwable cause) {
        super(cause);
    }
}
