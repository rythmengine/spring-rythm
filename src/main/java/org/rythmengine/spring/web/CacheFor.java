package org.rythmengine.spring.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation on a controller handling method to indicate the
 * view returned by the handling method will be cached
 *
 * <p>If a time is not specified, the results will be cached for 1 hour by default.
 *
 * <p>Example: <code>@CacheFor("1h")</code>
 *
 * <p>If <code>rythm.cache.prodOnly</code> configuration is true,
 * then cache will not effect on dev mode</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheFor {
    /**
     * Cache time. Support the following type:
     * <ul>
     * <li>1s: one second</li>
     * <li>2mn: two minutes</li>
     * <li>3h: three hours</li>
     * <li>foreaver: never expire</li>
     * </ul>
     *
     * @return cache expiration time
     */
    String value() default "1h";

    /**
     * Define the cache key. Leave it empty if you want system to generate key
     * from request automatically
     *
     * @return the session key provider class
     */
    Class<? extends CacheKeyProvider> key() default CacheKeyProvider.Default.class;

    /**
     * Whether use session id to generate the cache key, meaning if the cached copy is 
     * for session specific or not
     * <p>default: false</p>
     *
     * @return true if it is session sensitive
     */
    boolean sessionSensitive() default false;
    
    
    /**
     * Whether use current locale to generate the cache key.
     * <p>Default: false</p>
     *
     * @return true if it is language sensitive
     */
    boolean langSensitive() default false;

    /**
     * Whether the cache key is sensitive to request.secure. When this parameter
     * is set to <code>true</code>, then the request coming from http and https channel
     * will result in different cached copy
     * 
     * <p>default: false</p>
     * 
     * @return true if key is http scheme sensitive
     */
    boolean schemeSensitive() default false;

    /**
     * Indicate whether cache post request. Useful for certain case,
     * e.g. facebook always post to tab page in iframe
     * <p>default: false</p>
     * 
     * @return true if cache post request also 
     */
    boolean cachePost() default false;

}
