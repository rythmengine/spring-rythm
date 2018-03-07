package org.rythmengine.spring.web.util;

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
