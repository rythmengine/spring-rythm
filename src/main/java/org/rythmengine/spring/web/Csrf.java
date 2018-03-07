package org.rythmengine.spring.web;

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

import org.osgl.util.S;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * Created by luog on 6/12/13.
 */
public class Csrf {
    public static final String DEFAULT_PARAMETER_NAME = "__csrf";
    public static final String DEFAULT_HEADER_NAME = "__csrf";
    public static final String SESSION_KEY = "__rythm_csrf";

    public final String parameterName;
    public final String headerName;
    public final String value;

    public Csrf(String parameterName, String headerName) {
        if (S.blank(parameterName)) {
            parameterName = DEFAULT_PARAMETER_NAME;
        }
        this.parameterName = parameterName;
        if (S.blank(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }
        this.headerName = headerName;
        this.value = Session.current().getAuthenticityToken();
    }

    public Csrf(String parameterName, String headerName, HttpSession session) {
        if (S.blank(parameterName)) {
            parameterName = DEFAULT_PARAMETER_NAME;
        }
        this.parameterName = parameterName;
        if (S.blank(headerName)) {
            headerName = DEFAULT_HEADER_NAME;
        }
        this.headerName = headerName;
        String s = (String)session.getAttribute(SESSION_KEY);
        if (null == s) {
            s = UUID.randomUUID().toString();
            session.setAttribute(SESSION_KEY, s);
        }
        this.value = s;
    }

    public boolean check(String token) {
        return S.eq(token, value);
    }

    public void addToSession(HttpSession session) {
        session.setAttribute(SESSION_KEY, value);
    }

}
