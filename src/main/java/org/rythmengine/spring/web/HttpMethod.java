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

import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;

/**
 * Created by luog on 6/12/13.
 */
public enum HttpMethod {
    GET, HEAD, POST, DELETE, PUT, PATCH, TRACE, OPTIONS, CONNECT;

    private String id;
    private HttpMethod() {id = name().intern();}

    private static EnumSet<HttpMethod> unsafeMethods = EnumSet.of(POST, DELETE, PUT, PATCH);

    public boolean safe() {
        return !unsafe();
    }

    public boolean unsafe() {
        return unsafeMethods.contains(this);
    }

    public static HttpMethod valueOf(HttpServletRequest request) {
        String rm = request.getMethod().intern();
        for (HttpMethod m : values()) {
            if (m.id == rm) {
                return m;
            }
        }
        throw new RuntimeException("Oops, http request's method[" + request.getMethod() + "] not recognized");
    }

    public static boolean notSafe(HttpServletRequest request) {
        return valueOf(request).unsafe();
    }

    public static boolean isSafe(HttpServletRequest request) {
        return !notSafe(request);
    }

}
