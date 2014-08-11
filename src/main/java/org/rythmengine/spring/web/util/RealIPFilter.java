package org.rythmengine.spring.web.util;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * From http://www.lacerta.be/d7/content/keeping-real-user-ip-java-web-apps-behind-nginx-proxy
 */
public class RealIPFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            chain.doFilter(new RealIPRequestWrapper(request), response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
