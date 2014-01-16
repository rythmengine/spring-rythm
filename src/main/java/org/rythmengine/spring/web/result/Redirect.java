package org.rythmengine.spring.web.result;

import org.osgl.util.E;
import org.osgl.util.S;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by luog on 30/12/13.
 */
public class Redirect extends Result {

    /**
     * @param url the url to redirect
     */
    public Redirect(String url) {
        super(HttpStatus.TEMPORARY_REDIRECT, url);
        E.npeIf(S.empty(url));
    }

    @Override
    protected void writeToResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.sendRedirect(getMessage());
    }
}
