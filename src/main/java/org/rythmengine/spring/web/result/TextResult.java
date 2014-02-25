package org.rythmengine.spring.web.result;

import org.osgl.util.IO;
import org.osgl.util.S;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by luog on 26/02/14.
 */
public class TextResult extends Result {
    private String text;
    public TextResult(String text) {
        super(HttpStatus.OK);
        this.text = text;
    }

    public TextResult(String fmt, Object... args) {
        this(S.fmt(fmt, args));
    }

    @Override
    protected ModelAndView writeToResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        IO.writeContent(text, response.getWriter());

        return new ModelAndView();
    }
}
