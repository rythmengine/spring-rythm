package org.rythmengine.spring.web.util;

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

import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.S;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by luog on 3/01/14.
 */
public class StandardServletMultipartResolver extends org.springframework.web.multipart.support.StandardServletMultipartResolver {
    @Override
    public boolean isMultipart(HttpServletRequest request) {
        // support HTTP PUT operation
        String method = request.getMethod().toLowerCase();
        if (S.neq("post", method) && S.neq("put", method)) {
            return false;
        }
        String contentType = request.getContentType();
        return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
    }
}
