package org.rythmengine.spring.web;

/*-
 * #%L
 * Spring Rythm Plugin
 * %%
 * Copyright (C) 2017 - 2018 OSGL (Open Source General Library)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
