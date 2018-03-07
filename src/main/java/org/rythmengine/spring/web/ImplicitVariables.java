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

import org.osgl.util.E;
import org.osgl.web.util.UserAgent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: luog
 * Date: 27/01/12
 * Time: 10:23 AM
 * To change this template use File | Settings | File Templates.
 */
class ImplicitVariables {

    static class Var {
        String name;
        String type;
        boolean underscoreImplicitVariableName = false;

        Var(String name, String type, boolean underscoreImplicitVariableName) {
            this.name = name;
            this.type = type;
            this.underscoreImplicitVariableName = underscoreImplicitVariableName;
        }

        String name() {
            return underscoreImplicitVariableName ? "_" + name : name;
        }

        protected Object evaluate() {
            throw E.unsupport();
//            Map<String, Object> renderArgs = RythmView.renderArgs.get();
//            if (null == renderArgs) return null;
//            return renderArgs.get(name());
        }
    }

    List<Var> vars;

    ImplicitVariables(boolean underscoreImplicitVariableName) {
        Var[] vars = {
                new Var("request", HttpServletRequest.class.getName(), underscoreImplicitVariableName),
                new Var("response", HttpServletResponse.class.getName(), underscoreImplicitVariableName),
                new Var("httpSession", HttpSession.class.getName(), underscoreImplicitVariableName),
                new Var("session", Session.class.getName(), underscoreImplicitVariableName),
                new Var("userAgent", UserAgent.class.getName(), underscoreImplicitVariableName),
                new Var("flash", Flash.class.getName(), underscoreImplicitVariableName),
                new Var("csrf", Csrf.class.getName(), underscoreImplicitVariableName),
                new Var("__request", HttpServletRequest.class.getName(), false),
        };
        this.vars = Arrays.asList(vars);
    }

}
