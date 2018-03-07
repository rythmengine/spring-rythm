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
