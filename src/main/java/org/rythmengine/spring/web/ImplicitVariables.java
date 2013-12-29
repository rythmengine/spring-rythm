package org.rythmengine.spring.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            Map<String, Object> renderArgs = RythmView.renderArgs.get();
            if (null == renderArgs) return null;
            return renderArgs.get(name());
        }
    }

    List<Var> vars;

    ImplicitVariables(boolean underscoreImplicitVariableName) {
        Var[] vars = {
                new Var("request", HttpServletRequest.class.getName(), underscoreImplicitVariableName),
                new Var("response", HttpServletResponse.class.getName(), underscoreImplicitVariableName),
                new Var("httpSession", HttpSession.class.getName(), underscoreImplicitVariableName),
                new Var("session", Session.class.getName(), underscoreImplicitVariableName),
                new Var("flash", Flash.class.getName(), underscoreImplicitVariableName),
                new Var("csrf", Csrf.class.getName(), underscoreImplicitVariableName),
                new Var("__request", HttpServletRequest.class.getName(), false),
        };
        this.vars = Arrays.asList(vars);
    }

}
