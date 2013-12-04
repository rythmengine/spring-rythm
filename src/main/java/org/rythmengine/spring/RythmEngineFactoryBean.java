package org.rythmengine.spring;

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
 * @see #setSettings
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

