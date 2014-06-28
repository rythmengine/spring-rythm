package org.rythmengine.spring.web;

import org.osgl.web.util.UserAgent;
import org.rythmengine.utils.S;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 14/01/14.
 */
public class UADetector extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserAgent> current = new ThreadLocal<UserAgent>();

    public static final UserAgent get() {
        return current.get();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String s = request.getHeader(HttpHeaders.USER_AGENT);
        if (S.notEmpty(s)) {
            UserAgent ua = UserAgent.valueOf(s);
            current.set(ua);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        current.remove();
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        current.remove();
    }
}
