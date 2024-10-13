package com.study.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final StringBuilder messageBuilder = new StringBuilder();
    private HttpStatus status;
    private String location;
    private Object responseBody;
    private String contentType = "text/html; charset=UTF-8";

    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();

    public HttpResponse() {
    }

    public String getResponse() throws IOException {
        if (isHtmlResponseBody()) {
            this.responseBody = findResourceFromLocation((String) responseBody);
            updateHtmlHeaders();
        } else {
            this.responseBody = convertToJsonIfNecessary(responseBody);
            updateJsonHeaders();
        }

        generateStatusLine();
        generateHeaderLine(); // 헤더, 쿠키 처리
        messageBuilder.append(System.lineSeparator());

        if (responseBody != null) {
            messageBuilder.append(responseBody);
        }

        return messageBuilder.toString();
    }

    private boolean isHtmlResponseBody() {
        return this.responseBody instanceof String && ((String) this.responseBody).endsWith(".html");
    }

    private Object convertToJsonIfNecessary(Object responseBody) throws IOException {
        if (!(responseBody instanceof String)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(responseBody);
        }
        return responseBody;
    }


    private void updateHtmlHeaders() {
        headers.put("Content-Type", "text/html; charset=UTF-8");
        headers.put("Content-Length", String.valueOf(responseBody.toString().getBytes().length));
    }


    private void updateJsonHeaders() {
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put("Content-Length", String.valueOf(responseBody.toString().getBytes().length));
    }


    // 헤더, 쿠키
    private void generateHeaderLine() {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            messageBuilder.append(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
        }

        // 쿠키 추가
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            messageBuilder.append(String.format("Set-Cookie: %s=%s\r\n", cookie.getKey(), cookie.getValue()));
        }
    }

    private void generateStatusLine() {
        messageBuilder.append(String.format("HTTP/1.1 %s %s\r\n", status.getStatusCode(), status.getReason()));
    }

    private static String findResourceFromLocation(String location) throws IOException {
        URL resource = ClassLoader.getSystemClassLoader().getResource("static" + location);
        if (resource == null) {
            throw new FileNotFoundException("Resource not found: " + location);
        }

        Path path = Paths.get(resource.getPath());
        return Files.readString(path);
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setLocation(String location) {
        this.location = location;
        headers.put("Location", location);
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public void setContentType(String contentType) {
        headers.put("Content-Type", contentType);
    }

    public void setContentLength(int length) {
        headers.put("Content-Length", String.valueOf(length));
    }

    public void addCookie(String key, String value) {
        cookies.put(key, value);
    }
}
