package org.rythmengine.spring.web;

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
