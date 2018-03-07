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
