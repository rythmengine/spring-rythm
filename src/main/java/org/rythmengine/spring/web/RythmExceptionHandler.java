package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;
import org.rythmengine.spring.web.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by luog on 4/12/13.
 */
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@EnableWebMvc
public class RythmExceptionHandler implements MessageSourceAware {

    private static MessageSource messageSource;

    public static MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    RythmEngine engine;

    @Autowired
    public RythmExceptionHandler(RythmConfigurer conf) {
        this.engine = conf.getRythmEngine();
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(Exception e, HttpServletResponse response) throws Exception {
        if (e instanceof Result) {
            Result r = (Result)e;
            SessionManager._save();
            HttpServletRequest request = SessionManager.request();
            r.apply(request, response);
            return new ModelAndView();
        }
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (null != responseStatus) {
            SessionManager._save();
            int statusCode = responseStatus.value().value();
            String reason = responseStatus.reason();
            if (this.messageSource != null) {
                reason = this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale());
            }
            if (!StringUtils.hasLength(reason)) {
                response.sendError(statusCode);
            } else {
                response.sendError(statusCode, reason);
            }
            return new ModelAndView();
        }
        if (engine.isProdMode() || e instanceof ServletException) {
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
