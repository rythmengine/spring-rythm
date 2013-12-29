package org.rythmengine.spring.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 30/12/13.
 */
public enum HttpUtils {
    ;
    /**
     * The encoding that should be used when writing this response to the client
     */
    public static String getEncoding(HttpServletResponse response) {
        return response.getCharacterEncoding();
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static HttpFormat resolveFormat(HttpServletRequest request) {
        return HttpFormat.resolve(request);
    }
}
