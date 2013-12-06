package org.rythmengine.spring.web;

import org.rythmengine.utils.S;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;

public interface CacheKeyProvider {

    String getKey(boolean sessionSensitive, boolean schemeSensitive, boolean langSensitive);

    public static class Default implements CacheKeyProvider {

        protected final HttpServletRequest req() {
            ServletRequestAttributes sra = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            return sra.getRequest();
        }

        protected final HttpSession session(HttpServletRequest req) {
            return req.getSession(false);
        }

        @Override
        public String getKey(boolean sessionSensitive, boolean schemeSensitive, boolean langSensitive) {
            HttpServletRequest req = req();
            String key = "rythm" + req.getRequestURI() + S.str(req.getQueryString());
            if (sessionSensitive) {
                HttpSession sess = session(req);
                if (null != sess) {
                    key += sess.getId();
                }
            }
            if (schemeSensitive) {
                key += req.isSecure() ? "1" : "0";
            }
            if (langSensitive) {
                Locale locale = LocaleContextHolder.getLocale();
                if (null != locale) {
                    key += locale.toString();
                }
            }
            return key;
        }
    };
}
