package org.rythmengine.spring.web;

import org.osgl._;
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

    public static abstract class InternalServerErrorVisitor extends _.Visitor<Exception> {}

    private static MessageSource messageSource;

    public static MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    RythmEngine engine;

    boolean customErrorPages;

    private InternalServerErrorVisitor internalServerErrorVisitor;

    @Autowired
    public RythmExceptionHandler(RythmConfigurer conf) {
        this.engine = conf.getRythmEngine();
        this.customErrorPages = conf.customErrorPages;
    }

    @Autowired(required = false)
    public void setInternalServerErrorVisitor(InternalServerErrorVisitor errorVisitor) {
        internalServerErrorVisitor = errorVisitor;
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView defaultErrorHandler(Exception e, HttpServletResponse response) throws Exception {
        if (e instanceof Result) {
            Result r = (Result)e;
            HttpServletRequest request = SessionManager.request();
            return r.apply(request, response);
        }
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (null != responseStatus) {
            SessionManager._save();
            int statusCode = responseStatus.value().value();
            boolean isError = Result.isError(statusCode);
            String reason = responseStatus.reason();
            if (this.messageSource != null) {
                reason = this.messageSource.getMessage(reason, null, reason, LocaleContextHolder.getLocale());
            }
            ModelAndView modelAndView = new ModelAndView();
            if (isError) {
                modelAndView.addObject("statusCode", statusCode);
                modelAndView.addObject("message", reason);
                modelAndView.addObject("attachment", e);
                modelAndView.setViewName("errors/prod/error.html");
                return modelAndView;
            }
            if (!StringUtils.hasLength(reason)) {
                response.sendError(statusCode);
            } else {
                response.sendError(statusCode, reason);
            }
            return new ModelAndView();
        }
        if (engine.isProdMode() || e instanceof ServletException) {
            if (null != internalServerErrorVisitor) {
                internalServerErrorVisitor.visit(e);
            }
            if (customErrorPages) {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.addObject("statusCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                modelAndView.addObject("message", "Internal server error");
                modelAndView.addObject("attachment", e);
                modelAndView.setViewName("errors/prod/error.html");
                return modelAndView;
            }
            throw e;
        }
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", e);
        if (engine.isProdMode()) {
            mav.addObject("statusCode", 500);
            mav.addObject("message", "Internal server error");
            mav.addObject("attachment", e);
            mav.setViewName("errors/prod/error.html");
        } else {
            mav.setViewName("errors/500.html");
        }
        if (null != internalServerErrorVisitor) {
            internalServerErrorVisitor.visit(e);
        }
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
