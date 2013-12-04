package org.rythmengine.spring;

import org.rythmengine.utils.IO;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 25/11/13
 * Time: 1:40 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Version {
    ;
    public static final String VALUE;
    static {
        VALUE = IO.readContentAsString(Version.class.getClassLoader().getResourceAsStream("spring-rythm-version"));
    }
}
