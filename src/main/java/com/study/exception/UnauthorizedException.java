package com.study.exception;

public class UnauthorizedException extends ServletException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
