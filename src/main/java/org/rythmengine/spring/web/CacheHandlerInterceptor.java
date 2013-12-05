package org.rythmengine.spring.web;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 5/12/13.
 */
public class CacheHandlerInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware {

    private ApplicationContext ctx;

    private CacheFor getCacheAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod hm = (HandlerMethod)handler;
        return hm.getMethodAnnotation(CacheFor.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CacheFor c = getCacheAnnotation(handler);
        if (null == c) {
            return true;
        }
        String m = request.getMethod().toUpperCase();
        if ("GET".equals(m) || "HEAD".equals(m) || (c.cachePost() && "POST".equals(m))) {

        }
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.ctx = applicationContext;
    }
}
