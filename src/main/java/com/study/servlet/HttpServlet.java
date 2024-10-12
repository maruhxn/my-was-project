package com.study.servlet;

import com.study.exception.ServletException;
import com.study.request.HttpRequest;
import com.study.response.HttpResponse;

import java.io.IOException;

public interface HttpServlet {

    void service(HttpRequest request, HttpResponse response) throws ServletException, IOException;
}
