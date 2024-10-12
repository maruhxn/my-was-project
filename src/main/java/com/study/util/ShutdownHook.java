package com.study.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownHook implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);

    private final ServerSocket serverSocket;
    private final ExecutorService es;

    public ShutdownHook(ServerSocket serverSocket, ExecutorService es) {
        this.serverSocket = serverSocket;
        this.es = es;
    }

    @Override
    public void run() {
        log.info("shutdownHook 실행");
        try {
            shutdownAndAwaitTermination(es);
            serverSocket.close();
            Thread.sleep(1000); // 자원 정리 대기
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void shutdownAndAwaitTermination(ExecutorService es) {
        es.shutdown();
        try {
            log.info("서비스 정상 종료 시도");
            if (!es.awaitTermination(5, TimeUnit.SECONDS)) {
                log.info("서비스 정상 종료 실패 -> 강제 종료 시도");
                es.shutdownNow();
                if (!es.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.info("서비스가 종료되지 않았습니다.");
                }
            }
        } catch (InterruptedException ex) {
            es.shutdownNow();
        }
    }
}
