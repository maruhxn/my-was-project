package com.study.session;

import java.util.HashMap;
import java.util.Map;

public class HttpCookie {

    private static final String DOMAIN = "Domain";
    private static final String MAX_AGE = "Max-Age";
    private static final String PATH = "Path";
    private static final String SECURE = "Secure";
    private static final String HTTP_ONLY = "HttpOnly";

    private final String name;
    private String value;

    private Map<String, String> attributes = new HashMap<>();

    public HttpCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public void setMaxAge(int expiry) {
        this.attributes.put(MAX_AGE, expiry < 0 ? null : String.valueOf(expiry));
    }

    public int getMaxAge() {
        String maxAge = this.attributes.get(MAX_AGE);
        return maxAge == null ? -1 : Integer.parseInt(maxAge);
    }

    public void setDomain(String domain) {
        this.attributes.put(DOMAIN, domain != null ? domain.toLowerCase() : null);
    }

    public String getDomain() {
        return this.attributes.get(DOMAIN);
    }

    public void setPath(String uri) {
        this.attributes.put(PATH, uri);
    }

    public String getPath() {
        return this.attributes.get(PATH);
    }

    public void setSecure(boolean flag) {
        this.attributes.put(SECURE, String.valueOf(flag));
    }

    public boolean getSecure() {
        return Boolean.parseBoolean(this.attributes.get(SECURE));
    }

    public void setHttpOnly(boolean httpOnly) {
        this.attributes.put(HTTP_ONLY, String.valueOf(httpOnly));
    }

    public boolean isHttpOnly() {
        return Boolean.parseBoolean(this.attributes.get(HTTP_ONLY));
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        StringBuilder cookieBuilder = new StringBuilder();

        // 기본 name=value 형식 추가
        if (value != null) {
            cookieBuilder.append(value);
        }

        // Domain
        if (attributes.get(DOMAIN) != null) {
            cookieBuilder.append("; ").append(DOMAIN).append("=").append(attributes.get(DOMAIN));
        }

        // Max-Age
        if (attributes.get(MAX_AGE) != null) {
            cookieBuilder.append("; ").append(MAX_AGE).append("=").append(attributes.get(MAX_AGE));
        }

        // Path
        if (attributes.get(PATH) != null) {
            cookieBuilder.append("; ").append(PATH).append("=").append(attributes.get(PATH));
        }

        // Secure (true인 경우 "Secure"만 추가)
        if (getSecure()) {
            cookieBuilder.append("; ").append(SECURE);
        }

        // HttpOnly (true인 경우 "HttpOnly"만 추가)
        if (isHttpOnly()) {
            cookieBuilder.append("; ").append(HTTP_ONLY);
        }

        return cookieBuilder.toString();
    }

    public String getRawValue() {
        return value;
    }
}
