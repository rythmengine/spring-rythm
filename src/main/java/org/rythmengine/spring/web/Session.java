package org.rythmengine.spring.web;

import org.rythmengine.spring.util.Cache;
import org.rythmengine.utils.S;

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

    Map<String, String> data = new HashMap<String, String>(); // ThreadLocal access
    boolean changed = false;

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
            String s = Cache.get(key);
            if (null == s) {
                s = UUID.randomUUID().toString();
                change();
            }
            Cache.put(key, s, "1h");
            return s;
        }
        throw new IllegalStateException("Rythm session manager not enabled");
    }

    void clearAuthenticityToken() {
        String key = atKey();
        Cache.delete(key);
    }

    static boolean checkAuthenticityToken(String token) {
        String key = current().atKey();
        String s = Cache.get(key);
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
        Cache.put(sessionedKey(key), val);
    }

    public void cache(String key, Object val, String expiration) {
        Cache.put(sessionedKey(key), val, expiration);
    }

    public void cacheFor1Hr(String key, Object val) {
        Cache.put(sessionedKey(key), val, "1hr");
    }

    public void cacheFor30Min(String key, Object val) {
        Cache.put(sessionedKey(key), val, "30mn");
    }

    public void cacheFor10Min(String key, Object val) {
        Cache.put(sessionedKey(key), val, "10mn");
    }

    public void cacheFor5Min(String key, Object val) {
        Cache.put(sessionedKey(key), val, "5mn");
    }

    public void cacheFor1Min(String key, Object val) {
        Cache.put(sessionedKey(key), val, "1mn");
    }

    public void evict(String key) {
        Cache.delete(key);
    }

    public <T> T cached(String key) {
        return (T) Cache.get(sessionedKey(key));
    }

    public <T> T cached(String key, Class<T> clz) {
        return (T) Cache.get(sessionedKey(key));
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
