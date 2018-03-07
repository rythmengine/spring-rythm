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

import org.osgl.cache.CacheService;
import org.osgl.cache.CacheServiceProvider;
import org.osgl.util.S;
import org.rythmengine.spring.util.CacheServiceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implement a stateless session. The logic comes from Play!Framework
 * Created by luog on 7/12/13.
 */
public class Session {

    static final String AT_KEY = "___AT";
    static final String ID_KEY = "___ID";
    static final String TS_KEY = "___TS";
    static final String FP_KEY = "___FP";
    static final String EXPIRE_KEY = "___EXPIRED";

    Map<String, String> data = new HashMap<String, String>(); // ThreadLocal access
    boolean changed = false;

    private static volatile CacheService cache;

    private static CacheService cache() {
        if (null != cache) {
            return cache;
        }
        synchronized (Session.class) {
            if (null == cache) {
                cache = CacheServiceProvider.Impl.Auto.get();
                CacheServiceRegistry.register(cache);
            }
        }
        return cache;
    }

    public static Session current() {
        return SessionManager.session();
    }

    public String getId() {
        if (!data.containsKey(ID_KEY)) {
            data.put(ID_KEY, UUID.randomUUID().toString());
        }
        return data.get(ID_KEY);
    }

    public Map<String, String> all() {
        return data;
    }

    private String atKey() {
        String sessId = Session.current().getId();
        return sessId + AT_KEY;
    }

    private boolean sessionManagerEnabled() {
        return RythmConfigurer.getInstance().sessionManagerEnabled();
    }

    String getAuthenticityToken() {
        if (sessionManagerEnabled()) {
            String key = atKey();
            String s = cache().get(key);
            if (null == s) {
                s = UUID.randomUUID().toString();
                change();
            }
            cache().put(key, s, 60 * 60);
            return s;
        }
        throw new IllegalStateException("Rythm session manager not enabled");
    }

    void clearAuthenticityToken() {
        String key = atKey();
        cache().evict(key);
    }

    static boolean checkAuthenticityToken(String token) {
        String key = current().atKey();
        String s = cache().get(key);
        return S.eq(s, token);
    }

    void change() {
        changed = true;
    }

    public void put(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException("Character ':' is invalid in a session key.");
        }
        change();
        if (value == null) {
            data.remove(key);
        } else {
            data.put(key, value);
        }
    }

    public void put(String key, Object value) {
        change();
        if (value == null) {
            put(key, (String) null);
        }
        put(key, value + "");
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean remove(String key) {
        change();
        return data.remove(key) != null;
    }

    public void remove(String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }

    private String sessionedKey(String key) {
        return String.format("%s-%s", key, getId());
    }

    public void cache(String key, Object val) {
        cache().put(sessionedKey(key), val);
    }

    public void cache(String key, Object val, int expiration) {
        cache().put(sessionedKey(key), val, expiration);
    }

    public void cacheFor1Hr(String key, Object val) {
        cache().put(sessionedKey(key), val, 60 * 60);
    }

    public void cacheFor30Min(String key, Object val) {
        cache().put(sessionedKey(key), val, 60 * 30);
    }

    public void cacheFor10Min(String key, Object val) {
        cache().put(sessionedKey(key), val, 60 * 10);
    }

    public void cacheFor5Min(String key, Object val) {
        cache().put(sessionedKey(key), val, 60 * 5);
    }

    public void cacheFor1Min(String key, Object val) {
        cache().put(sessionedKey(key), val, 60);
    }

    public void evictCache(String key) {
        cache().evict(sessionedKey(key));
    }

    public <T> T cached(String key) {
        return cache().get(sessionedKey(key));
    }

    public <T> T cached(String key, Class<T> clz) {
        return cache().get(sessionedKey(key));
    }

    public void clear() {
        change();
        data.clear();
    }

    /**
     * Returns true if the session is empty,
     * e.g. does not contain anything else than the timestamp
     */
    public boolean isEmpty() {
        for (String key : data.keySet()) {
            if (!TS_KEY.equals(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    @Override
    public String toString() {
        return data.toString();
    }

}
