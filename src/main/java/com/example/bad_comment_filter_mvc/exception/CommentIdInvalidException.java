package com.example.bad_comment_filter_mvc.exception;

public class CommentIdInvalidException extends RuntimeException {
    public static final String ERR_MESSAGE = "Id must be start with 0, and consecutive.";
    public CommentIdInvalidException() {
        super(ERR_MESSAGE);
    }
}
