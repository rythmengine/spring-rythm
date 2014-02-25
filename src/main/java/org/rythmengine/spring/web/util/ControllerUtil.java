package org.rythmengine.spring.web.util;

import org.osgl.web.util.UserAgent;
import org.rythmengine.spring.web.*;
import org.rythmengine.spring.web.result.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * The base controller class provides utility methods to
 * send out response easily
 */
public abstract class ControllerUtil {

    protected final void notFoundIf(boolean predicate) {
        if (predicate) notFound();
    }

    protected final void notFoundIfNot(boolean predicate) {
        if (!predicate) notFound();
    }

    protected static NotFound notFound() {
        throw new NotFound();
    }

    protected static NotFound notFound(String reason) {
        throw new NotFound(reason);
    }

    protected static void notFoundIfNull(Object object) {
        if (null == object) notFound();
    }

    protected static void notFoundIfNull(Object object, String reason) {
        if (null == object) notFound(reason);
    }

    protected static void forbidden() {
        throw new Forbidden();
    }

    protected static void badRequest() {
        throw new BadRequest();
    }

    protected static void badRequest(String reason) {
        throw new BadRequest(reason);
    }

    protected static void badRequestIf(boolean expression) {
        if (expression) badRequest();
    }

    protected static void badRequestIf(boolean expression, String reason) {
        if (expression) badRequest(reason);
    }

    protected static void badRequestIfNot(boolean expression) {
        if (!expression) badRequest();
    }

    protected static void badRequestIfNot(boolean expression, String reason) {
        if (!expression) badRequest();
    }

    protected static void ok() {
        throw new Ok();
    }

    protected static void renderText(String text) {
        throw new TextResult(text);
    }

    protected static void renderText(String fmt, Object... args) {
        throw new TextResult(fmt, args);
    }


    protected static void setJSON() {
        // IE9 doesn't support application/json
        UserAgent ua = UADetector.get();
        response().setContentType(ua.isIE9Down() ? "text/plain" : "application/json");
    }

    protected static void renderJSON() {
        renderJSON("{}");
    }

    protected static void renderJSON(String json) {
        throw new JSONResult(json);
    }

    protected static void renderJSON(Object obj) {
        throw new JSONResult(obj);
    }

    protected static void renderJSON(Map<String, Object> map) {
        throw new JSONResult(map);
    }

    protected static void renderJSON(Object... objs) {
        throw new JSONResult(objs);
    }

    protected static void redirect(String url) {
        throw new Redirect(url);
    }

    protected static void setCookie(String name, String value) {
        SessionManager.setCookie(name, value);
    }

    protected static void setCookie(String name, String value, String expiration) {
        SessionManager.setCookie(name, value, expiration);
    }

    protected static void setCookie(String name, String value, String domain, String path, int maxAge, boolean secure) {
        SessionManager.setCookie(name, value, domain, path, maxAge, secure);
    }

    protected static HttpServletRequest request() {
        return SessionManager.request();
    }

    protected static HttpServletResponse response() {
        return SessionManager.response();
    }

    protected static boolean isAjax() {
        return HttpUtils.isAjax(request());
    }

    protected static final boolean isAjax(HttpServletRequest request) {
        return HttpUtils.isAjax(request);
    }

    protected static final Session session() {
        return Session.current();
    }

    protected static final Flash flash() {
        return Flash.current();
    }

}
