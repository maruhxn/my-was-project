package com.study.exception;

public class InternalServerException extends ServletException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(Throwable cause) {
        super(cause);
    }
}
