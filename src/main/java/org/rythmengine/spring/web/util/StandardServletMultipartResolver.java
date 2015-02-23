package org.rythmengine.spring.web.util;

import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by luog on 3/01/14.
 */
public class StandardServletMultipartResolver extends org.springframework.web.multipart.support.StandardServletMultipartResolver {
    @Override
    public boolean isMultipart(HttpServletRequest request) {
        // support HTTP PUT operation
        String method = request.getMethod().toLowerCase();
        if (S.neq("post", method) && S.neq("put", method)) {
            return false;
        }
        String contentType = request.getContentType();
        return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
    }
}
