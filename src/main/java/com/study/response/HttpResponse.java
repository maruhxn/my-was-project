package com.study.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final HttpStatus httpStatus;
    private final String requestURI;
    private final String location;
    private String responseBody;
    private final StringBuilder messageBuilder = new StringBuilder();
    private String contentType = "text/html; charset=UTF-8";

    public HttpResponse(HttpStatus httpStatus, String requestURI, String location) throws IOException {
        this.httpStatus = httpStatus;
        this.requestURI = requestURI;
        this.location = location;
    }

    public String getResponse() throws IOException {
        this.responseBody = findResourceFromRequestURI(location);

        generateStatusLine(httpStatus);
        generateHeaderLine(contentType, responseBody);
        messageBuilder.append(System.lineSeparator());
        messageBuilder.append(responseBody);
        return messageBuilder.toString();
    }

    private void generateHeaderLine(String contentType, String responseBody) {
        messageBuilder.append(String.format("Content-Type: %s\r\n", contentType));
        messageBuilder.append(String.format("Content-Length: %s\r\n", responseBody.getBytes(UTF_8).length));
    }

    private void generateStatusLine(HttpStatus httpStatus) {
        messageBuilder.append(String.format("HTTP/1.1 %s %s\r\n", httpStatus.getStatusCode(), httpStatus.getReason()));
    }

    private String findResourceFromRequestURI(String location) throws IOException {
        URL resource = ClassLoader.getSystemClassLoader().getResource("static" + location);
        if (resource == null) {
            throw new FileNotFoundException("Resource not found: " + location);
        }

        Path path = Paths.get(resource.getPath());
        return Files.readString(path);
    }
}
