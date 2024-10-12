package com.study.servlet;

import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.response.HttpStatus;

import java.io.IOException;

public class UnauthorizedServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED);
        response.setResponseBody("/401.html");
    }
}
