package org.rythmengine.spring.web;

import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.ListBuilder;
import org.osgl.util.S;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.regex.Pattern;

public class CsrfManager extends HandlerInterceptorAdapter {

    Logger logger = L.get(CsrfManager.class);

    private String parameterName = Csrf.DEFAULT_PARAMETER_NAME;
    private String headerName = Csrf.DEFAULT_HEADER_NAME;
    private List<Pattern> waiveList = C.list();

    void setParameterName(String parameterName) {
        if (S.notBlank(parameterName)) this.parameterName = parameterName;
    }

    void setHeaderName(String headerName) {
        if (S.notBlank(headerName)) this.headerName = headerName;
    }

    void setWaiveList(List<String> list) {
        if (list.isEmpty()) return;
        ListBuilder<Pattern> lb = ListBuilder.create();
        for (String s: list) {
            if (S.blank(s)) continue;
            s = s.trim().toLowerCase();
            lb.add(Pattern.compile(s, Pattern.CASE_INSENSITIVE));
        }
        waiveList = lb.toList();
    }

    private boolean shouldWaiveCsrfCheck(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (Pattern p: waiveList) {
            if (p.matcher(path).matches()) {
                return true;
            }
        }
        logger.debug("request url cannot be waived: %s", path);
        return false;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.isSafe(request) || shouldWaiveCsrfCheck(request)) return true;

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
                logger.error("Cannot verify the authenticity token for request: %s", request.getRequestURI());
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
            if (S.neq(s, token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bad authenticity token");
                return false;
            }
        }

        return true;
    }
}
