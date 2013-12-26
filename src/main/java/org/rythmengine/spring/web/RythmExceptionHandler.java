package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by luog on 4/12/13.
 */
@ControllerAdvice
@EnableWebMvc
public class RythmExceptionHandler {

    RythmEngine engine;

    @Autowired
    public RythmExceptionHandler(RythmConfigurer conf) {
        this.engine = conf.getRythmEngine();
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(Exception e, HttpServletResponse response) throws Exception {
        if (engine.isProdMode()) {
            throw e;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        mav.setViewName("errors/500.html");
        return mav;
    }

    public static enum Util {
        ;

        public static List<StackTraceElement> tail(Throwable t) {
            ArrayList<StackTraceElement> l = new ArrayList<StackTraceElement>(Arrays.asList(t.getStackTrace()));
            if (l.size() > 0) l.remove(0);
            if (l.size() > 15) {
                for (int i = l.size() - 1; i >= 15; --i) {
                    l.remove(i);
                }
            }
            return l;
        }

        public static StackTraceElement head(Throwable t) {
            StackTraceElement[] sa = t.getStackTrace();
            if (sa.length == 0) {
                return null;
            }
            return sa[0];
        }
    }
}
