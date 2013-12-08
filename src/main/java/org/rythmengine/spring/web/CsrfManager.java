package org.rythmengine.spring.web;

import org.rythmengine.utils.S;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by luog on 6/12/13.
 */
public class CsrfManager extends HandlerInterceptorAdapter {

    private String parameterName = Csrf.DEFAULT_PARAMETER_NAME;
    private String headerName = Csrf.DEFAULT_HEADER_NAME;

    void setParameterName(String parameterName) {
        if (S.notEmpty(parameterName)) this.parameterName = parameterName;
    }

    void setHeaderName(String headerName) {
        if (S.notEmpty(headerName)) this.headerName = headerName;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.isSafe(request)) return true;

        String token = request.getParameter(parameterName);
        if (null == token) {
            token = request.getHeader(headerName);
        }

// Facebook use POST to direct request to landing page, and obviously token is not there yet
//        if (null == token) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad authenticity token");
//            return false;
//        }

        if (RythmConfigurer.getInstance().sessionManagerEnabled()) {
            if (!Session.checkAuthenticityToken(token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad authenticity token");
                return false;
            }
        } else {
            HttpSession httpSession = request.getSession(false);
            if (null == httpSession) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad authenticity token");
                return false;
            }
            String s = (String)httpSession.getAttribute(Csrf.SESSION_KEY);
            if (S.ne(s, token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad authenticity token");
                return false;
            }
        }

        return true;
    }
}
