package com.study.request;

import com.study.session.Session;
import com.study.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private static final String COOKIE_HEADER_NAME = "Cookie";

    private HttpMethod method;
    private String requestURI;
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();

    private final SessionManager sessionManager;

    public HttpRequest(BufferedReader reader, SessionManager sessionManager) throws IOException {
        this.sessionManager = sessionManager;
        parseRequestLine(reader); // 먼저 시작 라인 파싱
        parseHeaders(reader); // 이후 헤더 파싱
        parseBody(reader);
    }

    private void parseRequestLine(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null) {
            throw new IOException("EOF: No request line received");
        }
        String[] parts = requestLine.split(" "); // -> { "GET", "/search?q=hello", "HTTP/1.1" }
        if (parts.length != 3) {
            throw new IOException("Invalid request line: " + requestLine);
        }
        method = HttpMethod.valueOf(parts[0]);
        String[] pathParts = parts[1].split("\\?");
        requestURI = pathParts[0];
        if (pathParts.length > 1) {
            parseQueryParameters(pathParts[1]);
        }
    }

    private void parseQueryParameters(String queryString) {
        for (String param : queryString.split("&")) {
            String[] keyValue = param.split("=");
            String key = URLDecoder.decode(keyValue[0], UTF_8);
            String value = keyValue.length > 1 ?
                    URLDecoder.decode(keyValue[1], UTF_8) : "";
            parameters.put(key, value);
        }
    }

    private void parseHeaders(BufferedReader reader) throws IOException {
        String line;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] headerParts = line.split(":");
            headers.put(headerParts[0].trim(), headerParts[1].trim());

            // Cookie 헤더 파싱
            if (headerParts[0].trim().equalsIgnoreCase(COOKIE_HEADER_NAME)) {
                parseCookies(headerParts[1].trim());
            }
        }
    }

    private void parseCookies(String cookieHeader) {
        String[] cookiePairs = cookieHeader.split(";");
        for (String cookie : cookiePairs) {
            String[] keyValue = cookie.split("=", 2); // 쿠키는 'name=value' 형태로 전달됨
            String key = keyValue[0].trim();
            String value = keyValue.length > 1 ? keyValue[1].trim() : ""; // value가 없는 경우 빈 문자열로 처리
            cookies.put(key, value);
        }
    }

    private void parseBody(BufferedReader reader) throws IOException {
        final String CONTENT_LENGTH_HEADER_KEY = "Content-Length";
        final String CONTENT_TYPE_HEADER_KEY = "Content-Type";

        if (!headers.containsKey(CONTENT_LENGTH_HEADER_KEY)) {
            return;
        }

        int contentLength = Integer.parseInt(headers.get(CONTENT_LENGTH_HEADER_KEY));
        char[] bodyChars = new char[contentLength];
        int read = reader.read(bodyChars);
        if (read != contentLength) {
            throw new IOException("Failed to read entire body. Expected " +
                    contentLength + " bytes, but read " + read);
        }

        String body = new String(bodyChars);

        String contentType = headers.get(CONTENT_TYPE_HEADER_KEY);
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            parseQueryParameters(body);
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                ", path='" + requestURI + '\'' +
                ", queryParameters=" + parameters +
                ", headers=" + headers +
                '}';
    }

    public Session getSession() {
        return sessionManager.getSession(this, true);
    }

    public Session getSession(boolean creation) {
        return sessionManager.getSession(this, creation);
    }
}
