package org.rythmengine.spring.web.result;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 30/12/13.
 */
public class Ok extends Result {

    /**
     * @param why a description of the problem
     */
    public Ok(String why) {
        super(HttpStatus.OK, why);
    }

    public Ok() {
        super(HttpStatus.OK);
    }

    @Override
    protected ModelAndView writeToResponse(HttpServletResponse response, int statusCode, String message) {
        // do nothing for 200 Okay
        return new ModelAndView();
    }
}
