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

    private static class Context {
        String serverName;
        int serverPort;
        boolean isSecure;
        String ctxPath;
    }

    private static volatile Context context;

    public static void setContext(HttpServletRequest request) {
        if (null != context) {
            return;
        }
        synchronized (ControllerUtil.class) {
            if (null != context) return;

            context = new Context();
            context.serverName = request.getServerName();
            context.serverPort = request.getServerPort();
            context.isSecure = request.isSecure();
            context.ctxPath = ServletContextHolder.getServletContext().getContextPath();
        }
    }

    public static String fullUrl(String url) {
        HttpServletRequest request = SessionManager.request();
        return fullUrl(url, request);
    }

    public static String fullUrl(String url, HttpServletRequest request) {
        if (null == url) url = "/";
        if (url.startsWith("http:") || url.startsWith("https:")) return url;
        if (null != request && !url.startsWith("/")) {
            StringBuffer sb = request.getRequestURL();
            sb.append("/").append(url);
            return sb.toString();
        }
        String scheme, serverName, ctxPath, reqPath = "";
        int port;
        if (null != request) {
            scheme = request.isSecure() ? "https://" : "http://";
            serverName = request.getServerName();
            ctxPath = request.getContextPath();
            port = request.getServerPort();
            reqPath = request.getRequestURI();
        } else {
            scheme = context.isSecure ? "https://" : "http://";
            serverName = context.serverName;
            ctxPath = context.ctxPath;
            port = context.serverPort;
        }
        StringBuilder sb = new StringBuilder(scheme);
        if (url.startsWith("//")) return sb.append(url.substring(2)).toString();
        sb.append(serverName);
        sb.append(":");
        sb.append(port);
        if (url.startsWith("/")) sb.append(ctxPath).append(url);
        else sb.append(ctxPath).append("/").append(url);
        return sb.toString();
    }

    public static String url(String url) {
        HttpServletRequest request = SessionManager.request();
        return url(url, request);
    }

    public static String url(String url, HttpServletRequest request) {
        if (url == null) return "/";
        if (url.startsWith("//") || url.startsWith("http:") || url.startsWith("https://")) {
            return url;
        }
        if (null != request && !url.startsWith("/")) {
            StringBuilder sb = new StringBuilder(request.getRequestURI());
            sb.append("/").append(url);
            return sb.toString();
        }
        String ctxPath = "";
        if (null != request) {
            ctxPath = request.getContextPath();
        } else if (null != context) {
            ctxPath = context.ctxPath;
        }
        StringBuilder sb = new StringBuilder(ctxPath);
        if (!url.startsWith("/")) sb.append("/");
        sb.append(url);
        return sb.toString();
    }

}
