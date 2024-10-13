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

    private static int MAX_THREADS = -1;
    private static int ACCEPT_COUNT = -1;
    private static int PORT = -1;

    public static void main(String[] args) {
        parseProgramArguments(args);

        Resource eTagResource = new Resource("v1", "v1");
        UserRepository userRepository = new MemoryUserRepository();
        List<Object> controllers = List.of(new UserController(userRepository, eTagResource));
        AnnotationServlet annotationServlet = new AnnotationServlet(controllers);

        ServletManager servletManager = new ServletManager();
        servletManager.setDefaultServlet(annotationServlet);
        servletManager.add("/favicon.ico", new DiscardServlet());

        SessionManager sessionManager = new SessionManager();

        Connector connector = new Connector(PORT, MAX_THREADS, ACCEPT_COUNT, servletManager, sessionManager);
        connector.start();
    }

    private static void parseProgramArguments(String[] args) {
        for (String arg : args) {
            String[] parameterEntry = arg.split("=");
            if (parameterEntry[0].equals("maxThreads")) {
                MAX_THREADS = Integer.parseInt(parameterEntry[1]);
            } else if (parameterEntry[0].equals("acceptCount")) {
                ACCEPT_COUNT = Integer.parseInt(parameterEntry[1]);
            } else if (parameterEntry[0].equals("port")) {
                PORT = Integer.parseInt(parameterEntry[1]);
            }
        }
    }
}
