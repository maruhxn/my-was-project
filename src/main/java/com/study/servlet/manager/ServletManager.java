package com.study.servlet.manager;

import com.study.exception.BadRequestException;
import com.study.exception.NotFoundException;
import com.study.exception.NotImplementedException;
import com.study.exception.UnauthorizedException;
import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServletManager {

    private static final Logger log = LoggerFactory.getLogger(ServletManager.class);

    private final Map<String, HttpServlet> servletMap = new HashMap<>();

    private HttpServlet defaultServlet;
    private HttpServlet notFoundErrorServlet = new NotFoundServlet();
    private HttpServlet badRequestServlet = new BadRequestServlet();
    private HttpServlet unauthorizedSerlet = new UnauthorizedServlet();
    private HttpServlet notImplementedServlet = new NotImplementedServlet();
    private HttpServlet internalErrorServlet = new InternalErrorServlet();

    public ServletManager() {
    }

    public void add(String path, HttpServlet servlet) {
        servletMap.put(path, servlet);
    }

    public void setDefaultServlet(HttpServlet defaultServlet) {
        this.defaultServlet = defaultServlet;
    }

    public void setNotFoundErrorServlet(HttpServlet notFoundErrorServlet) {
        this.notFoundErrorServlet = notFoundErrorServlet;
    }

    public void setInternalErrorServlet(HttpServlet internalErrorServlet) {
        this.internalErrorServlet = internalErrorServlet;
    }

    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        try {
            HttpServlet servlet = servletMap.getOrDefault(request.getRequestURI(), defaultServlet);
            if (servlet == null) { // default 등록 안했으면 없을 수 있음
                throw new NotFoundException("request url= " + request.getRequestURI());
            }

            servlet.service(request, response);
        } catch (BadRequestException e) {
            log.error(e.getMessage(), e);
            badRequestServlet.service(request, response);
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            unauthorizedSerlet.service(request, response);
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            notFoundErrorServlet.service(request, response);
        } catch (NotImplementedException e) {
            log.error(e.getMessage(), e);
            notImplementedServlet.service(request, response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            internalErrorServlet.service(request, response);
        }
    }
}
