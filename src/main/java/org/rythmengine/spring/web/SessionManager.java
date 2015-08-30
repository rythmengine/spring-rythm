package org.rythmengine.spring.web;

import org.osgl._;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.Crypto;
import org.osgl.util.E;
import org.osgl.util.S;
import org.rythmengine.spring.util.LongSession;
import org.rythmengine.spring.util.ShortSession;
import org.rythmengine.utils.Time;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionManager extends HandlerInterceptorAdapter {

    Logger logger = L.get(SessionManager.class);

    public static interface Listener {
        void onSessionResolved(Session session);
        void onSessionCleanUp();

        public static abstract class Base implements Listener, InitializingBean {
            @Override
            public void afterPropertiesSet() throws Exception {
                SessionManager.addListener(this);
            }
        }
    }

    public static final String ATTR_LONG_SESSION = "__long_sess__";
    public static final String DEFAULT_COOKIE_PREFIX = "WHLAB";
    public static final int DEFAULT_COOKIE_EXPIRE = 60 * 60 * 24 * 30;

    static final Pattern SESSION_PARSER = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static final Pattern FLASH_PARSER = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static final String AT_KEY = Session.AT_KEY;
    static final String ID_KEY = Session.ID_KEY;
    static final String TS_KEY = Session.TS_KEY;
    static final String FP_KEY = Session.FP_KEY;
    public static final String EXPIRE_KEY = Session.EXPIRE_KEY;

    private static String sessionCookieName = DEFAULT_COOKIE_PREFIX + "_SESSION";
    private static String flashCookieName = DEFAULT_COOKIE_PREFIX + "_FLASH";
    private static int ttl = -1;
    private static boolean cookieSecure = false;
    private static boolean noPersistentCookie = true;
    private static final C.List<Listener> listeners = C.newList();
    private static String pingPath;
    private static boolean useIpAffinity;

    public static void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    void setSessionPrefix(String prefix) {
        if (null != prefix) {
            sessionCookieName = prefix + "_SESSION";
            flashCookieName = prefix + "_FLASH";
        }
    }

    void setSessionExpire(String expire) {
        if (null != expire) ttl = Time.parseDuration(expire);
    }

    void setCookieSecure(boolean secure) {
        cookieSecure = secure;
    }

    void setNoPersistentCookie(boolean value) {
        noPersistentCookie = value;
    }

    void setPingPath(String uri) {
        pingPath = uri;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        req.set(request);
        resp.set(response);
        boolean longSession = isLongSession(handler);
        request.setAttribute(ATTR_LONG_SESSION, longSession);
        Cookie[] cookies = request.getCookies();
        Map<String, Cookie> m = new HashMap<String, Cookie>();
        Cookie sessionCookie = null;
        Cookie flashCookie = null;
        if (null != cookies) {
            for (Cookie c : cookies) {
                String nm = c.getName();
                if ("JSESSIONID".equalsIgnoreCase(c.getName())) continue; // ignore java http session
                // see http://stackoverflow.com/questions/20791557/chrome-sent-duplicate-cookie
                if (S.eq(sessionCookieName, nm)) sessionCookie = c;
                else if (S.eq(flashCookieName, nm)) flashCookie = c;
                else m.put(nm, c);
//                if (!sessionResolved && S.eq(sessionCookieName, nm)) {
//                    // pick up only the first cookie with the name COOKIE_PREFIX + "_SESSION"
//                    // see http://stackoverflow.com/questions/4056306/how-to-handle-multiple-cookies-with-the-same-name
//                    resolveSession(c);
//                    sessionResolved = true;
//                }
            }
        }
        String uri = request.getRequestURI();
        resolveSession(sessionCookie, uri, longSession);
        session().getAuthenticityToken();
        resolveFlash(flashCookie);
        cookie.set(m);
        return true;
    }

    private static Set<Object> longSessionRequests = C.newSet();
    private static Set<Object> shortSessionRequests = C.newSet();

    private static boolean isLongSession(Object handler) {
        if (shortSessionRequests.contains(handler)) {
            return false;
        }
        if (longSessionRequests.contains(handler)) {
            return true;
        }
        Method m;
        if (handler instanceof HandlerMethod) {
            m = ((HandlerMethod) handler).getMethod();
        } else if (handler instanceof Method) {
            m = (Method) handler;
        } else {
            throw E.unexpected("Unknown handler type: %s", handler.getClass());
        }
        if (AnnotationUtils.findAnnotation(m, ShortSession.class) != null) {
            shortSessionRequests.add(handler);
            return false;
        } else if (AnnotationUtils.findAnnotation(m, LongSession.class) != null) {
            longSessionRequests.add(handler);
            return true;
        } else {
            Class<?> c = m.getDeclaringClass();
            if (AnnotationUtils.findAnnotation(c, LongSession.class) != null) {
                longSessionRequests.add(handler);
                return true;
            } else {
                shortSessionRequests.add(handler);
                return false;
            }
        }
    }

    private static void persist(HttpServletRequest request, HttpServletResponse response) {
        boolean longSession = false;
        Object o = request.getAttribute(ATTR_LONG_SESSION);
        if (null != o) {
            longSession = Boolean.parseBoolean(o.toString());
        }
        saveSession(longSession);
        saveFlash();
        Map<String, Cookie> cookies = cookie.get();
        if (null == cookies) return;
        for (Cookie c : cookies.values()) {
            String cookieName = c.getName();
            // workaround for servlet 2.5 doesn't support httpOnly cookie issue
            if (S.eq(cookieName, flashCookieName) || S.eq(cookieName, sessionCookieName)) {
                writeHttpOnlyCookie(c, response);
            } else {
                response.addCookie(c);
            }
        }
    }

    private static void writeHttpOnlyCookie(Cookie cookie, HttpServletResponse resp) {
        StringBuffer sb = new StringBuffer();
        TomcatServerCookieHelper.appendCookieValue(sb, cookie.getVersion(), cookie.getName(), cookie.getValue(), cookie.getPath(), cookie.getDomain(), cookie.getComment(), cookie.getMaxAge(), cookie.getSecure(), true);
        resp.addHeader("Set-Cookie", sb.toString());
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        persist(request, response);
    }

//    public static void onRenderResult(HttpServletRequest request, HttpServletResponse response) {
//        persist(request, response);
//    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        cleanUp();
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        cleanUp();
    }

    private void cleanUp() {
        sess.remove();
        req.remove();
        fla.remove();
        cookie.remove();
        listeners.accept(F.ON_SESSION_CLEAN_UP);
    }

    public static String remoteAddress(HttpServletRequest req) {
        String ip = req.getHeader("X_FORWARDED_FOR");
        if (S.blank(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    private static String fingerPrint() {
        HttpServletRequest req = request();
        String ip = remoteAddress(req);
        String userAgent = req.getHeader(HttpHeaders.USER_AGENT);
        return UUID.nameUUIDFromBytes((ip + userAgent).getBytes()).toString();
    }

    private static void verifyFingerPrint(Session session) {
        if (!useIpAffinity) return;
        String storedFP = session.get(FP_KEY), validFP = fingerPrint();
        if (null != storedFP && S.neq(storedFP, validFP)) {
            // TODO verify finger print
        }
    }

    private void resolveSession(Cookie cookie, String uri, boolean longSession) throws Exception {
        Session session = new Session();
        final long expiration = ttl * 1000L;
        String value = null == cookie ? null : cookie.getValue();
        if (S.blank(value)) {
            // no previous cookie to restore; but we have to set the timestamp in the new cookie
            if (!longSession && ttl > -1) {
                session.put(TS_KEY, System.currentTimeMillis() + expiration);
            }
        } else {
            int firstDashIndex = value.indexOf("-");
            if(firstDashIndex > -1) {
                String sign = value.substring(0, firstDashIndex);
                String data = value.substring(firstDashIndex + 1);
                if (sign.equals(sign(data))) {
                    String sessionData = URLDecoder.decode(data, "utf-8");
                    Matcher matcher = SESSION_PARSER.matcher(sessionData);
                    while (matcher.find()) {
                        session.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
            if (!longSession && ttl > -1) {
                long newTimestamp = System.currentTimeMillis() + expiration;
                // Verify that the session contains a timestamp, and that it's not expired
                if (!session.contains(TS_KEY)) {
                    session = new Session();
                } else {
                    if (S.eq(pingPath, uri)) {
                        newTimestamp = Long.parseLong(session.get(TS_KEY));
                    }
                    if ((Long.parseLong(session.get(TS_KEY))) < System.currentTimeMillis()) {
                        // Session expired
                        session = new Session();
                        session.put(EXPIRE_KEY, true);
                    } else {
                        session.remove(EXPIRE_KEY);
                    }
                }
                session.put(TS_KEY, newTimestamp);
            }
        }
        sess.set(session);
        listeners.accept(F.onSessionResolved(session));
    }

    private static Cookie createSessionCookie(String value, boolean longSession) {
        Cookie cookie = new Cookie(sessionCookieName, value);
        cookie.setPath("/");
        cookie.setSecure(cookieSecure);
        if (!longSession && ttl > -1 && !noPersistentCookie) {
            cookie.setMaxAge(ttl);
        }
        return cookie;
    }

    static void _save() {
        boolean longSession = false;
        HttpServletRequest req = request();
        if (null != req) {
            Object o = req.getAttribute(ATTR_LONG_SESSION);
            if (null != o) {
                longSession = Boolean.parseBoolean(o.toString());
            }
        }
        saveSession(longSession);
        saveFlash();
    }

    private static void saveSession(boolean longSession) {
        Session session = session();
        if (null == session) {
            return;
        }
        if (!session.changed && ttl < 0) {
            // Nothing changed and no cookie-expire, consequently send nothing back.
            return;
        }

        Cookie sessionCookie;
        if (session.isEmpty()) {
            // session is empty, delete it from cookie
            sessionCookie = createSessionCookie("", longSession);
        } else {
            if (ttl > -1 && !session.contains(TS_KEY)) {
                // session get cleared before
                session.put(TS_KEY, System.currentTimeMillis() + ttl * 1000L);
            }
            StringBuilder sb = new StringBuilder();
            for (String k : session.data.keySet()) {
                sb.append("\u0000");
                sb.append(k);
                sb.append(":");
                sb.append(session.data.get(k));
                sb.append("\u0000");
            }
            try {
                String data = URLEncoder.encode(sb.toString(), "utf-8");
                String sign = sign(data);
                sessionCookie = createSessionCookie(sign + "-" + data, longSession);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("How come utf-8 is not recognized?");
            }
        }
        SessionManager.cookie.get().put(sessionCookieName, sessionCookie);
    }

    private void resolveFlash(Cookie cookie) {
        Flash flash = new Flash();
        if (null != cookie) {
            try {
                String s = URLDecoder.decode(cookie.getValue(), "utf-8");
                Matcher matcher = FLASH_PARSER.matcher(s);
                while (matcher.find()) {
                    flash.data.put(matcher.group(1), matcher.group(2));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        fla.set(flash);
    }

    private static Cookie createFlashCookie(String value) {
        Cookie cookie = new Cookie(flashCookieName, value);
        cookie.setPath("/");
        if (ttl > -1 && !noPersistentCookie) {
            cookie.setMaxAge(ttl);
        }
        cookie.setSecure(cookieSecure);
        return cookie;
    }

    private static void saveFlash() {
        Flash flash = flash();
        if (null == flash) {
            return;
        }
        Map<String, String> out = flash.out;
        Cookie flashCookie;
        if (out.isEmpty()) {
            flashCookie = createFlashCookie("");
        } else {
            try {
                StringBuilder sb = new StringBuilder();
                for (String key : out.keySet()) {
                    sb.append("\u0000");
                    sb.append(key);
                    sb.append(":");
                    sb.append(out.get(key));
                    sb.append("\u0000");
                }
                String flashData = URLEncoder.encode(sb.toString(), "utf-8");
                flashCookie = createFlashCookie(flashData);
            } catch (Exception e) {
                throw new RuntimeException("Flash serializationProblem", e);
            }
        }
        cookie.get().put(flashCookieName, flashCookie);
    }

    public static String sign(String s) {
        return Crypto.sign(s, RythmConfigurer.getSecretKey().getBytes());
    }

    private static ThreadLocal<HttpServletRequest> req = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> resp = new ThreadLocal<HttpServletResponse>();
    private static ThreadLocal<Session> sess = new ThreadLocal<Session>();
    private static ThreadLocal<Flash> fla = new ThreadLocal<Flash>();
    private static ThreadLocal<Map<String, Cookie>> cookie = new ThreadLocal<Map<String, Cookie>>();

    public static Session session() {
        return sess.get();
    }

    public static HttpServletRequest request() {
        return req.get();
    }

    public static HttpServletResponse response() {
        return resp.get();
    }

    public static HttpSession httpSession() {
        return req.get().getSession(false);
    }

    public static Flash flash() {
        return fla.get();
    }

    public static void setCookie(String name, String value) {
        setCookie(name, value, null, "/", ttl, false);
    }

    public static void setCookie(String name, String value, String expiration) {
        setCookie(name, value, null, "/", Time.parseDuration(expiration), false);
    }

    public static void setCookie(String name, String value, String domain, String path, int maxAge, boolean secure) {
        Map<String, Cookie> map = SessionManager.cookie.get();
        if (map.containsKey(name)) {
            Cookie cookie = map.get(name);
            cookie.setValue(value);
            cookie.setSecure(secure);
            if (maxAge > -1) {
                cookie.setMaxAge(maxAge);
            }
        } else {
            Cookie cookie = new Cookie(name, value);
            if (null != domain) cookie.setDomain(domain);
            if (null != path) cookie.setPath(path);
            if (maxAge > -1) cookie.setMaxAge(maxAge);
            cookie.setSecure(secure);
            map.put(name, cookie);
        }
    }

    public static boolean hasCookie(String name) {
        Map<String, Cookie> map = cookie.get();
        return map.containsKey(name);
    }

    public static enum F {
        ;
        public static _.Visitor<Listener> onSessionResolved(final Session session) {
            return new _.Visitor<Listener>() {
                @Override
                public void visit(Listener listener) throws _.Break {
                    listener.onSessionResolved(session);
                }
            };
        }
        public static _.Visitor<Listener> ON_SESSION_CLEAN_UP = new _.Visitor<Listener>() {
            @Override
            public void visit(Listener listener) throws _.Break {
                listener.onSessionCleanUp();
            }
        };
    }
}
