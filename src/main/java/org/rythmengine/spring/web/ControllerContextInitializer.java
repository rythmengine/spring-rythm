package org.rythmengine.spring.web;

import org.rythmengine.spring.web.util.ControllerUtil;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 12/03/14.
 */
public class ControllerContextInitializer extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ControllerUtil.setContext(request);
        return true;
    }
}
