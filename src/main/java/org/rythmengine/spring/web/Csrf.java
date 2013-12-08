package org.rythmengine.spring.web;

import org.rythmengine.utils.S;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by luog on 6/12/13.
 */
public class Csrf {
    public static final String DEFAULT_PARAMETER_NAME = "__csrf";
    public static final String DEFAULT_HEADER_NAME = "__csrf";
    public static final String SESSION_KEY = "__rythm_csrf";

    public final String parameterName;
    public final String headerName;
    public final String value;

    public Csrf(String parameterName, String headerName) {
        if (S.empty(parameterName)) {
            parameterName = DEFAULT_PARAMETER_NAME;
        }
        this.parameterName = parameterName;
        if (S.empty(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }
        this.headerName = headerName;
        this.value = Session.current().getAuthenticityToken();
    }

    public Csrf(String parameterName, String headerName, HttpSession session) {
        if (S.empty(parameterName)) {
            parameterName = DEFAULT_PARAMETER_NAME;
        }
        this.parameterName = parameterName;
        if (S.empty(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }
        this.headerName = headerName;
        String s = (String)session.getAttribute(SESSION_KEY);
        if (null == s) {
            s = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY, s);
        }
        this.value = s;
    }

    public boolean check(String token) {
        return S.eq(token, value);
    }

    public void addToSession(HttpSession session) {
        session.setAttribute(SESSION_KEY, value);
    }

}
