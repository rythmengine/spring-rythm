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

import org.springframework.stereotype.Controller;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

/**
 * Created by luog on 11/03/14.
 */
@Controller
public class ServletContextHolder implements ServletContextAware {

    private static ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        ServletContextHolder.servletContext = servletContext;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}
