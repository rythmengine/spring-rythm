package org.rythmengine.spring.web.result;

import org.rythmengine.exception.FastRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by luog on 30/12/13.
 */
@ResponseStatus(HttpStatus.OK)
public class Ok extends FastRuntimeException {

    /**
     * @param why a description of the problem
     */
    public Ok(String why) {
        super(why);
    }

    public Ok() {}

}
