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

import org.osgl.util.S;
import org.rythmengine.RythmEngine;
import org.rythmengine.utils.IO;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CacheInterceptor extends HandlerInterceptorAdapter {

    static class KeyAndTTL {
        String key;
        String ttl;
    }

    private RythmEngine engine;

    private Map<Object, CacheFor> cacheForCache = new HashMap<Object, CacheFor>();
    private Map<Object, CacheKeyProvider> keyProviders = new HashMap<Object, CacheKeyProvider>();
    private Set<Object> blackList = new HashSet<Object>();

    private static final ThreadLocal<KeyAndTTL> cacheKey = new ThreadLocal<KeyAndTTL>();

    private CacheFor getCacheAnnotation(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        if (blackList.contains(handler)) {
            return null;
        }
        CacheFor c = cacheForCache.get(handler);
        if (null != c) {
            return c;
        }
        HandlerMethod hm = (HandlerMethod)handler;
        c = hm.getMethodAnnotation(CacheFor.class);
        if (null == c) {
            blackList.add(handler);
            return null;
        }
        cacheForCache.put(handler, c);
        return c;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CacheFor c = getCacheAnnotation(handler);
        if (null == c) {
            return true;
        }
        String m = request.getMethod().toUpperCase();
        if (!("GET".equals(m) || "HEAD".equals(m) || (c.cachePost() && "POST".equals(m)))) {
            return true;
        }

        CacheKeyProvider p = keyProviders.get(handler);
        if (null == p) {
            Class<? extends CacheKeyProvider> kpFact = c.key();
            p = kpFact.newInstance();
            keyProviders.put(handler, p);
        }

        String key = p.getKey(c.sessionSensitive(), c.schemeSensitive(), c.langSensitive());
        if (S.blank(key)) {
            return true;
        }
        Object o = engine.cached(key);
        if (null == o) {
            KeyAndTTL kt = new KeyAndTTL();
            kt.key = key;
            kt.ttl = c.value();
            cacheKey.set(kt);
            return true;
        } else {
            IO.writeContent(o.toString(), response.getWriter());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        cacheKey.remove();
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        cacheKey.remove();
    }

    public void setEngine(RythmEngine engine) {
        this.engine = engine;
    }

    public final static KeyAndTTL currentCacheKey() {
        return cacheKey.get();
    }
}
