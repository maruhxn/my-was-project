package com.study.exception;

public class PageNotFoundException extends RuntimeException {

    public PageNotFoundException(String message) {
        super(message);
    }
}
