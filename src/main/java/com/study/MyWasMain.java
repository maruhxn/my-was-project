package com.study;

import com.study.app.MemoryUserRepository;
import com.study.app.Resource;
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
        Resource eTagResource = new Resource("v1", "v1");
        UserRepository userRepository = new MemoryUserRepository();
        List<Object> controllers = List.of(new UserController(userRepository, eTagResource));
        AnnotationServlet annotationServlet = new AnnotationServlet(controllers);

        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(annotationServlet);
        servletManager.add("/favicon.ico", new DiscardServlet());

        SessionManager sessionManager = new SessionManager();

        Connector connector = new Connector(8080, 100, servletManager, sessionManager);
        connector.start();
    }
}
