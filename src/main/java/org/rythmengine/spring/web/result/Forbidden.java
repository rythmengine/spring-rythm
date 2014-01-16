package org.rythmengine.spring.web.result;

import org.springframework.http.HttpStatus;

/**
 * Created by luog on 30/12/13.
 */
public class Forbidden extends Result {

    /**
     * @param why a description of the problem
     */
    public Forbidden(String why) {
        super(HttpStatus.FORBIDDEN, why);
    }

    public Forbidden() {
        super(HttpStatus.FORBIDDEN);
    }

}
