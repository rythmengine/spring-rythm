package org.rythmengine.spring.web.result;

/*-
 * #%L
 * Spring Rythm Plugin
 * %%
 * Copyright (C) 2017 - 2018 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
