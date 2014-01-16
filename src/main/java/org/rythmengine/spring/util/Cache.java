package org.rythmengine.spring.util;

import org.rythmengine.RythmEngine;
import org.rythmengine.spring.web.RythmConfigurer;
import org.rythmengine.utils.Time;

/**
 * Created by luog on 8/12/13.
 */
public enum Cache {
;

    private static RythmEngine r() {
        return RythmConfigurer.engine();
    }

    /**
     * Add an element only if it doesn't exist.
     * @param key Element key
     * @param value Element value
     * @param expiration Ex: 10s, 3mn, 8h
     */
    public static void put(String key, Object value, String expiration) {
        r().cache(key, value, Time.parseDuration(expiration));
    }

    /**
     * Add an element only if it doesn't exist and store it indefinitely.
     * @param key Element key
     * @param value Element value
     */
    public static void put(String key, Object value) {
        r().cache(key, value, Time.parseDuration(null));
    }
    /**
     * Retrieve an object.
     * @param key The element key
     * @return The element value or null
     */
    public static <T> T get(String key) {
        return (T)r().cached(key);
    }

    /**
     * Delete an element from the cache.
     * @param key The element key
     */
    public static void delete(String key) {
        r().evict(key);
    }

    /**
     * Convenient clazz to get a value a class type;
     * @param <T> The needed type
     * @param key The element key
     * @param clazz The type class
     * @return The element value or null
     */
    @SuppressWarnings("unchecked")
	public static <T> T get(String key, Class<T> clazz) {
        return (T) r().cached(key);
    }

}
