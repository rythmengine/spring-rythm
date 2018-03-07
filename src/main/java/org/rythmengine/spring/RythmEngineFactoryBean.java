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

import org.rythmengine.RythmEngine;
import org.rythmengine.exception.RythmException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;

import java.io.IOException;

/**
 * Factory bean that configures a RythmEngine and provides it as bean
 * reference. This bean is intended for any kind of usage of Rythm in
 * application code, e.g. for generating email content. For web views,
 * RythmConfigurer is used to set up a RythmEngine for views.
 *
 * <p>The simplest way to use this class is to specify a "resourceLoaderPath";
 * you do not need any further configuration then. For example, in a web
 * application context:
 *
 * <pre class="code"> &lt;bean id="RythmEngine" class="org.rythmengine.spring.RythmEngineFactoryBean"&gt;
 *   &lt;property name="resourceLoaderPath" value="/WEB-INF/rythm/"/&gt;
 * &lt;/bean&gt;</pre>
 *
 * See the base class RythmEngineFactory for configuration details.
 *
 * @author Gelin Luo
 * @see #setConfigLocation
 * @see #setEngineConfig
 * @see #setResourceLoaderPath
 * @see org.rythmengine.spring.web.RythmConfigurer
 */
public class RythmEngineFactoryBean extends RythmEngineFactory
		implements FactoryBean<RythmEngine>, InitializingBean, ResourceLoaderAware {

	private RythmEngine engine;


	public void afterPropertiesSet() throws IOException, RythmException {
		this.engine = createRythmEngine();
	}


	public RythmEngine getObject() {
		return this.engine;
	}

	public Class<? extends RythmEngine> getObjectType() {
		return RythmEngine.class;
	}

	public boolean isSingleton() {
		return true;
	}

}

