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

import org.osgl.util.E;
import org.osgl.util.S;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Redirect extends Result {

    /**
     * @param url the url to redirect
     */
    public Redirect(String url) {
        super(HttpStatus.TEMPORARY_REDIRECT, url);
        E.npeIf(S.blank(url));
    }

    public Redirect(String url, Object ... args) {
        super(HttpStatus.TEMPORARY_REDIRECT, S.fmt(url, args));
        E.npeIf(S.blank(url));
    }

    @Override
    protected ModelAndView writeToResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        //response.sendRedirect(getMessage());
        return new ModelAndView("redirect:" + getMessage());
    }
}
