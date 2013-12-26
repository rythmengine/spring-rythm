package org.rythmengine.spring.web;

import org.rythmengine.spring.utils.Crypto;
import org.rythmengine.utils.S;
import org.rythmengine.utils.Time;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by luog on 7/12/13.
 */
public class SessionManager extends HandlerInterceptorAdapter {

    public static final String DEFAULT_COOKIE_PREFIX = "WHLAB";
    public static final int DEFAULT_COOKIE_EXPIRE = 60 * 60 * 24 * 30;

    static final Pattern SESSION_PARSER = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    static final String AT_KEY = "___AT";
    static final String ID_KEY = "___ID";
    static final String TS_KEY = "___TS";

    private String cookieName = DEFAULT_COOKIE_PREFIX + "_SESSION";
    private static int ttl = -1;
    void setSessionPrefix(String prefix) {
        this.cookieName = prefix + "_SESSION";
    }

    void setSessionExpire(String expire) {
        if (null != expire) ttl = Time.parseDuration(expire);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        req.set(request);
        Cookie[] cookies = request.getCookies();
        Map<String, Cookie> m = new HashMap<String, Cookie>();
        boolean sessionResolved = false;
        if (null != cookies) {
            for (Cookie c : cookies) {
                String nm = c.getName();
                m.put(nm, c);
                if (!sessionResolved && S.eq(cookieName, nm)) {
                    // pick up only the first cookie with the name COOKIE_PREFIX + "_SESSION"
                    // see http://stackoverflow.com/questions/4056306/how-to-handle-multiple-cookies-with-the-same-name
                    resolveSession(c);
                    sessionResolved = true;
                }
            }
        }
        if (!sessionResolved) {
            resolveSession(null);
        }
        session().getAuthenticityToken();
        cookie.set(m);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        saveSession(request);
        for (Cookie c : cookie.get().values()) {
            if ("JSESSIONID".equalsIgnoreCase(c.getName())) continue;
            response.addCookie(c);
        }
    }

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
        cookie.remove();
    }

    private void resolveSession(Cookie cookie) throws Exception {
        Session session = new Session();
        final long expiration = ttl * 1000L;
        String value = null == cookie ? null : cookie.getValue();
        if (S.empty(value)) {
            // no previous cookie to restore; but we may have to set the timestamp in the new cookie
            if (ttl > -1) {
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
            if (ttl > -1) {
                // Verify that the session contains a timestamp, and that it's not expired
                if (!session.contains(TS_KEY)) {
                    session = new Session();
                } else {
                    if ((Long.parseLong(session.get(TS_KEY))) < System.currentTimeMillis()) {
                        // Session expired
                        session = new Session();
                    }
                }
                session.put(TS_KEY, System.currentTimeMillis() + expiration);
            }
        }
        sess.set(session);
    }

    private void saveSession(HttpServletRequest request) {
        Session session = session();
        if (null == session) {
            return;
        }
        if (!session.changed && ttl < 0) {
            // Nothing changed and no cookie-expire, consequently send nothing back.
            return;
        }
        if (session.isEmpty()) {
            // session is empty, delete it from cookie
            createSessionCookie(request, "");
        } else {
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
                createSessionCookie(request, sign + "-" + data);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("How come utf-8 is not recognized?");
            }
        }
    }

    private void createSessionCookie(HttpServletRequest request, String value) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setPath(request.getContextPath());
        if (ttl > -1) {
            cookie.setMaxAge(ttl);
        }
        SessionManager.cookie.get().put(cookieName, cookie);
    }

    public static String sign(String s) {
        return Crypto.sign(s);
    }

    private static InheritableThreadLocal<HttpServletRequest> req = new InheritableThreadLocal<HttpServletRequest>();
    private static InheritableThreadLocal<Session> sess = new InheritableThreadLocal<Session>();
    private static InheritableThreadLocal<Map<String, Cookie>> cookie = new InheritableThreadLocal<Map<String, Cookie>>();

    public static Session session() {
        return sess.get();
    }

    public static HttpServletRequest request() {
        return req.get();
    }

    public static HttpSession httpSession() {
        return req.get().getSession(false);
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
        }
    }

}
