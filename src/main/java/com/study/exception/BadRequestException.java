package com.study.exception;

public class BadRequestException extends ServletException {
    public BadRequestException(String message) {
        super(message);
    }
}
