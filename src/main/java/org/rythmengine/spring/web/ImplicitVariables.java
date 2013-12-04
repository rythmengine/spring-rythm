package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;

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
                new Var("session", HttpSession.class.getName(), underscoreImplicitVariableName),
                new Var("rythm", RythmEngine.class.getName(), underscoreImplicitVariableName) {
                    @Override
                    String name() {
                        return "rythm";
                    }

                    @Override
                    protected Object evaluate() {
                        Map<String, Object> renderArgs = RythmView.renderArgs.get();
                        if (null == renderArgs) return null;
                        return renderArgs.get("_rythm");
                    }
                },
                new Var("_rythm", RythmEngine.class.getName(), underscoreImplicitVariableName) {
                    @Override
                    String name() {
                        return "_rythm";
                    }

                    @Override
                    protected Object evaluate() {
                        Map<String, Object> renderArgs = RythmView.renderArgs.get();
                        if (null == renderArgs) return null;
                        return renderArgs.get("_rythm");
                    }
                }

                // TODO: add Locale
        };
        this.vars = Arrays.asList(vars);
    }

}
