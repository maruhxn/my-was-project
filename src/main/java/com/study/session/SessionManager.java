package com.study.session;

import com.study.request.HttpRequest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private Map<String, Session> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 조회
     */
    public Session getSession(HttpRequest request, Boolean creation) {
        HttpCookie sessionCookie = request.getCookie(SESSION_COOKIE_NAME);

        // 세션 ID가 없으면 새로운 세션을 생성
        if (sessionCookie == null) {
            return creation ? createNewSession() : null;
        }

        String sessionId = sessionCookie.getRawValue();

        // 세션 저장소에서 세션을 가져옴
        Session session = sessionStore.get(sessionId);

        // 세션이 없으면 새로운 세션을 생성하거나 null 반환
        return (session == null && creation) ? createNewSession() : session;
    }

    private Session createNewSession() {
        Session newSession = new Session(UUID.randomUUID().toString());
        sessionStore.put(newSession.getId(), newSession);
        return newSession;
    }

}
