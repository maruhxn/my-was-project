package com.study.connector;

import com.study.handler.HttpRequestHandler;
import com.study.servlet.manager.ServletManager;
import com.study.session.SessionManager;
import com.study.util.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 커넥터는 클라이언트와 통신을 담당 -> HTTP 요청을 받아들이고 처리하는 역할
 * HTTP/1.1 표준에 따라 클라이언트로부터의 요청을 처리
 * 특정 포트(기본적으로 8080)에서 요청을 기다림
 */
public class Connector {

    private static final Logger log = LoggerFactory.getLogger(Connector.class);

    private final ExecutorService es = Executors.newFixedThreadPool(10);
    private final int DEFAULT_PORT = 8080;
    private final int DEFAULT_ACCEPT_COUNT = 100;
    private final ServerSocket serverSocket;
    private final ServletManager servletManager;
    private final SessionManager sessionManager;

    public Connector(final int port, final int acceptCount, ServletManager servletManager, SessionManager sessionManager) {
        this.servletManager = servletManager;
        this.serverSocket = createServerSocket(port, acceptCount);
        this.sessionManager = sessionManager;
    }

    private ServerSocket createServerSocket(int port, int acceptCount) {
        try {
            final int checkedPort = checkPort(port);
            final int checkedAcceptCount = checkAcceptCount(acceptCount);
            return new ServerSocket(checkedPort, checkedAcceptCount);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int checkPort(int port) {
        final int MIN_PORT = 1;
        final int MAX_PORT = 65535;

        if (port < MIN_PORT || MAX_PORT < port) {
            return DEFAULT_PORT;
        }
        return port;
    }

    private int checkAcceptCount(int acceptCount) {
        return Math.max(acceptCount, DEFAULT_ACCEPT_COUNT);
    }

    public void start() {
        log.info("서버 시작 {} port", serverSocket.getLocalPort());
        addShutdownHook();
        running();
    }

    private void addShutdownHook() {
        ShutdownHook target = new ShutdownHook(serverSocket, es);
        Runtime.getRuntime().addShutdownHook(new Thread(target, "shutdown"));
    }

    private void running() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                es.submit(new HttpRequestHandler(socket, servletManager, sessionManager));
            }
        } catch (IOException e) {
            log.error("서버 소켓 종료: {}", e.getMessage());
        }
    }

}
