package com.study.exception;

public class NotFoundException extends ServletException {

    public NotFoundException(String message) {
        super(message);
    }
}
