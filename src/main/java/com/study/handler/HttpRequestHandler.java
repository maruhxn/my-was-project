package com.study.handler;

import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.servlet.manager.ServletManager;
import com.study.session.SessionManager;
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
    private final ServletManager servletManager;
    private final SessionManager sessionManager;

    public HttpRequestHandler(Socket socket, ServletManager servletManager, SessionManager sessionManager) {
        this.socket = socket;
        this.servletManager = servletManager;
        this.sessionManager = sessionManager;
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
            HttpRequest request = new HttpRequest(reader, sessionManager);
            HttpResponse response = new HttpResponse();

            log.info("HTTP 요청: {}", request);

            servletManager.execute(request, response);
            outputStream.write(response.getResponse().getBytes());
            outputStream.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }


}
