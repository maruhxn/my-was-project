package com.study.servlet;

import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.response.HttpStatus;

import java.io.IOException;

public class DiscardServlet implements HttpServlet {
    @Override
    public void service(HttpRequest request, HttpResponse response) throws IOException {
        response.setStatus(HttpStatus.OK);
    }
}
