package org.rythmengine.spring.web.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * From http://www.lacerta.be/d7/content/keeping-real-user-ip-java-web-apps-behind-nginx-proxy
 */
public class RealIPRequestWrapper extends HttpServletRequestWrapper {
	public RealIPRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getRemoteAddr() {
		String realIP = super.getHeader("X-Real-IP");
		return realIP != null ? realIP : super.getRemoteAddr();
	}

	@Override
	public String getRemoteHost() {
		try {
			return InetAddress.getByName(getRemoteAddr()).getHostName();
		} catch (UnknownHostException e) {
			return getRemoteAddr();
		}
	}
}