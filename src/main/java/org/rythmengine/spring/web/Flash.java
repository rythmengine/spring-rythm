package org.rythmengine.spring.web;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by luog on 28/12/13.
 */
public class Flash {
    Map<String, String> data = new HashMap<String, String>();
    Map<String, String> out = new HashMap<String, String>();
    static Pattern flashParser = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");


    public static Flash current() {
        return SessionManager.flash();
    }

    public void put(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException("Character ':' is invalid in a flash key.");
        }
        data.put(key, value);
        out.put(key, value);
    }

    public void put(String key, Object value) {
        if (value == null) {
            put(key, (String) null);
        }
        put(key, value + "");
    }

    public void now(String key, String value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException("Character ':' is invalid in a flash key.");
        }
        data.put(key, value);
    }

    public void error(String value, Object... args) {
        put("error", String.format(value, args));
    }

    public String error() {
        return get("error");
    }

    public void success(String value, Object... args) {
        put("success", String.format(value, args));
    }

    public String success() {
        return get("success");
    }

    public void discard(String key) {
        out.remove(key);
    }

    public void discard() {
        out.clear();
    }

    public void keep(String key) {
        if (data.containsKey(key)) {
            out.put(key, data.get(key));
        }
    }

    public void keep() {
        out.putAll(data);
    }

    public String get(String key) {
        return data.get(key);
    }

    public boolean remove(String key) {
        return data.remove(key) != null;
    }

    public void clear() {
        data.clear();
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }

    @Override
    public String toString() {
        return data.toString();
    }

}
