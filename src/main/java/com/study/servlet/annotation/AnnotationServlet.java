package com.study.servlet.annotation;

import com.study.exception.InternalServerException;
import com.study.exception.NotFoundException;
import com.study.exception.ServletException;
import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.servlet.HttpServlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationServlet implements HttpServlet {

    private final Map<String, ControllerMethod> pathMap;

    public AnnotationServlet(List<Object> controllers) {
        this.pathMap = new HashMap<>();
        initializePathMap(controllers);
    }

    private void initializePathMap(List<Object> controllers) {
        for (Object controller : controllers) {
            for (Method method : controller.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    String path = method.getAnnotation(RequestMapping.class).value();
                    // 중복 경로 체크
                    if (pathMap.containsKey(path)) {
                        ControllerMethod controllerMethod = pathMap.get(path);
                        throw new IllegalArgumentException("경로 중복 등록, path="
                                + path + ", method=" + method + ", 이미 등록된 메서드=" + controllerMethod.method);
                    }
                    pathMap.put(path, new ControllerMethod(controller, method));
                }
            }
        }
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        ControllerMethod controllerMethod = pathMap.get(requestURI);

        if (controllerMethod == null) {
            throw new NotFoundException("request = " + requestURI);
        }

        controllerMethod.invoke(request, response);
    }

    private static class ControllerMethod {

        private final Object controller;
        private final Method method;

        public ControllerMethod(Object controller, Method method) {
            this.controller = controller;
            this.method = method;
        }

        public void invoke(HttpRequest request, HttpResponse response) throws ServletException {

            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] args = new Object[parameterTypes.length]; // request, response

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == HttpRequest.class) {
                    args[i] = request;
                } else if (parameterTypes[i] == HttpResponse.class) {
                    args[i] = response;
                } else {
                    throw new IllegalArgumentException("Unsupported parameter type: " + parameterTypes[i]);
                }

            }

            try {
                method.invoke(controller, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Throwable cause = e.getCause();

                if (cause instanceof ServletException) {
                    throw (ServletException) cause;
                } else {
                    throw new InternalServerException(e);
                }
            }
        }
    }
}
