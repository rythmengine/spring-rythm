package org.rythmengine.spring;

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

import org.rythmengine.utils.IO;
import org.springframework.core.SpringVersion;

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

    public static final String SPRING_VERSION = SpringVersion.getVersion();
}
