package org.rythmengine.spring.web.util;

import org.springframework.stereotype.Controller;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * Created by luog on 11/03/14.
 */
@Controller
public class ServletContextHolder implements ServletContextAware {

    private static ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        ServletContextHolder.servletContext = servletContext;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}
