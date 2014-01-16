package org.rythmengine.spring.web.util;

import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.osgl.util.S;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by luog on 3/01/14.
 */
public class CommonsMultipartResolver extends org.springframework.web.multipart.commons.CommonsMultipartResolver {
    @Override
    public boolean isMultipart(HttpServletRequest request) {
        // support HTTP PUT operation
        String method = request.getMethod().toLowerCase();
        if (S.neq("post", method) && S.neq("put", method)) {
            return false;
        }
        return FileUploadBase.isMultipartContent(new ServletRequestContext(request));
    }
}
