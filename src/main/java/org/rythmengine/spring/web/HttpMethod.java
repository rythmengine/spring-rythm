package org.rythmengine.spring.web;

import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;

/**
 * Created by luog on 6/12/13.
 */
public enum HttpMethod {
    GET, HEAD, POST, DELETE, PUT, PATCH, TRACE, OPTIONS, CONNECT;

    private String id;
    private HttpMethod() {id = name().intern();}

    private static EnumSet<HttpMethod> unsafeMethods = EnumSet.of(POST, DELETE, PUT, PATCH);

    public boolean safe() {
        return !unsafe();
    }

    public boolean unsafe() {
        return unsafeMethods.contains(this);
    }

    public static HttpMethod valueOf(HttpServletRequest request) {
        String rm = request.getMethod().intern();
        for (HttpMethod m : values()) {
            if (m.id == rm) {
                return m;
            }
        }
        throw new RuntimeException("Oops, http request's method[" + request.getMethod() + "] not recognized");
    }

    public static boolean notSafe(HttpServletRequest request) {
        return valueOf(request).unsafe();
    }

    public static boolean isSafe(HttpServletRequest request) {
        return !notSafe(request);
    }

}
