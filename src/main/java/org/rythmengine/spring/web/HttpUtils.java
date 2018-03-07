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
import javax.servlet.http.HttpServletResponse;

/**
 * Created by luog on 30/12/13.
 */
public enum HttpUtils {
    ;
    /**
     * The encoding that should be used when writing this response to the client
     */
    public static String getEncoding(HttpServletResponse response) {
        return response.getCharacterEncoding();
    }

    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static HttpFormat resolveFormat(HttpServletRequest request) {
        return HttpFormat.resolve(request);
    }

}
