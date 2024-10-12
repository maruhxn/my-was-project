package com.study;

import com.study.app.MemoryUserRepository;
import com.study.app.UserController;
import com.study.app.UserRepository;
import com.study.connector.Connector;
import com.study.servlet.DiscardServlet;
import com.study.servlet.annotation.AnnotationServlet;
import com.study.servlet.manager.ServletManager;
import com.study.session.SessionManager;

import java.util.List;

public class MyWasMain {
    public static void main(String[] args) {
        List<Object> controllers = List.of();
        AnnotationServlet annotationServlet = new AnnotationServlet(controllers);

        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(annotationServlet);
        servletManager.add("/favicon.ico", new DiscardServlet());


        Connector connector = new Connector(8080, 100, servletManager);
        connector.start();
    }
}
