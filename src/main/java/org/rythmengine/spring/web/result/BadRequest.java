package org.rythmengine.spring.web.result;

import org.springframework.http.HttpStatus;

/**
 * Created by luog on 30/12/13.
 */
public class BadRequest extends Result {

    /**
     * @param why a description of the problem
     */
    public BadRequest(String why) {
        super(HttpStatus.BAD_REQUEST, why);
    }

    public BadRequest() {
        super(HttpStatus.BAD_REQUEST);
    }

}
