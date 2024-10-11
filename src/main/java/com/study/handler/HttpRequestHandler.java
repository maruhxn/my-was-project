package com.study.handler;

import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.response.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class HttpRequestHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private final Socket socket;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void process() {
        try (
                socket;
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream outputStream = socket.getOutputStream()

        ) {
            HttpRequest request = new HttpRequest(reader);

            log.info("HTTP 요청: {}", request);

            HttpResponse response = null;
            String requestURI = request.getRequestURI();
            if (requestURI.equals("/login")) {
                response = new HttpResponse(HttpStatus.OK, requestURI, "/login.html");
            } else if (requestURI.equals("/register")) {
                response = new HttpResponse(HttpStatus.OK, requestURI, "/register.html");
            } else if (requestURI.equals("/")) {
                response = new HttpResponse(HttpStatus.OK, requestURI, "/index.html");
            }

            if (response == null) {
                response = new HttpResponse(HttpStatus.NOT_FOUND, requestURI, "/404.html");
            }

            outputStream.write(response.getResponse().getBytes());
            outputStream.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


}
