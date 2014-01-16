package org.rythmengine.spring.web.util;

import org.rythmengine.spring.web.RythmConfigurer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartResolver;

/**
 * Created by luog on 3/01/14.
 */
public class MultipartFilter extends org.springframework.web.multipart.support.MultipartFilter{

    private final MultipartResolver defaultMultipartResolver = new StandardServletMultipartResolver();

    /**
   	 * Look for a MultipartResolver bean in the root web application context.
   	 * Supports a "multipartResolverBeanName" filter init param; the default
   	 * bean name is "filterMultipartResolver".
   	 * <p>This can be overridden to use a custom MultipartResolver instance,
   	 * for example if not using a Spring web application context.
   	 * @return the MultipartResolver instance, or {@code null} if none found
   	 */
   	@Override
   	protected MultipartResolver lookupMultipartResolver() {
   		WebApplicationContext wac = (WebApplicationContext)RythmConfigurer.getInstance().getApplicationContext();
   		String beanName = getMultipartResolverBeanName();
   		if (wac != null && wac.containsBean(beanName)) {
   			if (logger.isDebugEnabled()) {
   				logger.debug("Using MultipartResolver '" + beanName + "' for MultipartFilter");
   			}
   			return wac.getBean(beanName, MultipartResolver.class);
   		} else {
   			return this.defaultMultipartResolver;
   		}
   	}

}
