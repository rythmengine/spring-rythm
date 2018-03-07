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

import org.springframework.stereotype.Component;

/**
 * Created by luog on 19/03/14.
 */
@Component
public class ServerContext {

    private static String hostName;

    private static int port;

    private static int securePort;

    private static String contextPath;

    public static String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        ServerContext.hostName = hostName;
    }

    public static int getPort() {
        return port;
    }

    public void setPort(int port) {
        ServerContext.port = port;
    }

    public static int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        ServerContext.securePort = securePort;
    }

    public static String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        ServerContext.contextPath = contextPath;
    }

}
