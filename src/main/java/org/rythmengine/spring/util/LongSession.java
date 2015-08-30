package org.rythmengine.spring.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate request target to a controller or action method to be treated as long session
 * <p>Usually a system target an end user, e.g. shopping site product page should be treated as long session</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LongSession {
}
