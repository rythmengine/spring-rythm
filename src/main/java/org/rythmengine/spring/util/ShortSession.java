package org.rythmengine.spring.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate request target to a controller or action method to be treated as short session
 * <p>This annotation is usually used to mark an action method as short session that exists in a
 * controller class been marked as {@link LongSession}</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ShortSession {
}
