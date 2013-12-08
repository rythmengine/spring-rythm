package org.rythmengine.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;
import org.rythmengine.conf.RythmConfigurationKey;
import org.rythmengine.exception.RythmException;
import org.rythmengine.extension.ITemplateResourceLoader;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Factory that configures a RythmEngine. Can be used standalone,
 * but typically you will either use {@link RythmEngineFactoryBean}
 * for preparing a RythmEngine as bean reference, or
 * {@link org.rythmengine.spring.web.RythmConfigurer}
 * for web views.
 *
 * <p>The optional "configLocation" property sets the location of the Rythm
 * properties file, within the current application. Rythm properties can be
 * overridden via "engineConfig", or even completely specified locally,
 * avoiding the need for an external properties file.
 *
 * <p>The "resourceLoaderPath" property can be used to specify the Rythm
 * resource loader path via Spring's Resource abstraction, possibly relative
 * to the Spring application context.
 *
 * <p>If "overrideLogging" is true (the default), the RythmEngine will be
 * configured to log via Commons Logging.
 *
 * <p>The simplest way to use this class is to specify a
 * {@link #setResourceLoaderPath(String) "resourceLoaderPath"}; the
 * RythmEngine typically then does not need any further configuration.
 *
 * @author Juergen Hoeller
 * @see #setConfigLocation
 * @see #setEngineConfig
 * @see #setResourceLoaderPath
 * @see #createRythmEngine
 * @see RythmEngineFactoryBean
 * @see org.rythmengine.spring.web.RythmConfigurer
 * @see org.rythmengine.RythmEngine
 */
public class RythmEngineFactory extends ApplicationObjectSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private Resource configLocation;

	private final Map<String, Object> engineConfig = new HashMap<String, Object>();

	private String resourceLoaderPath;

	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	private Boolean devMode = null;

	private DevModeSensor devModeSensor;

	protected boolean enableCache = true;

    private Map<String, Object> usrCtx = new HashMap<String, Object>();


	/**
	 * Set the location of the Rythm config file.
	 * Alternatively, you can specify all properties locally.
	 * @see #setEngineConfig
	 * @see #setResourceLoaderPath
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

    /**
     * Turn on/off Rythm Cache. By default Rythm Cache is turned on
     * @param enableCache when {@code true} the cache is turned on, otherwise off
     */
    public void setEnableCache(boolean enableCache) {
        this.enableCache = enableCache;
    }

	/**
	 * Set Rythm engineConfig, like "file.resource.loader.path".
	 * Can be used to override values in a Rythm config file,
	 * or to specify all necessary properties locally.
	 * <p>Note that the Rythm resource loader path also be set to any
	 * Spring resource location via the "resourceLoaderPath" property.
	 * Setting it here is just necessary when using a non-file-based
	 * resource loader.
	 * @see #setEngineConfigMap
	 * @see #setConfigLocation
	 * @see #setResourceLoaderPath
	 */
	public void setEngineConfig(Properties engineConfig) {
		CollectionUtils.mergePropertiesIntoMap(engineConfig, this.engineConfig);
	}

	/**
	 * Set Rythm properties as Map, to allow for non-String values
	 * like "ds.resource.loader.instance".
	 * @see #setEngineConfig
	 */
	public void setEngineConfigMap(Map<String, Object> engineConfigMap) {
		if (engineConfigMap != null) {
			this.engineConfig.putAll(engineConfigMap);
		}
	}

	/**
	 * Set the Rythm resource loader path via a Spring resource location.
	 * Accepts multiple locations in Rythm's comma-separated path style.
	 * <p>When populated via a String, standard URLs like "file:" and "classpath:"
	 * pseudo URLs are supported, as understood by ResourceLoader. Allows for
	 * relative paths when running in an ApplicationContext.
	 * <p>Will define a path for the default Rythm resource loader with the name
	 * "file". If the specified resource cannot be resolved to a {@code java.io.File},
	 * a generic SpringResourceLoader will be used under the name "spring", without
	 * modification detection.
	 * <p>Note that resource caching will be enabled in any case. With the file
	 * resource loader, the last-modified timestamp will be checked on access to
	 * detect changes. With SpringResourceLoader, the resource will be cached
	 * forever (for example for class path resources).
	 * @see #setResourceLoader
	 * @see #setEngineConfig
	 * @see SpringResourceLoader
	 * @see org.rythmengine.resource.FileResourceLoader
	 */
	public void setResourceLoaderPath(String resourceLoaderPath) {
		this.resourceLoaderPath = resourceLoaderPath;
	}

	/**
	 * Set the Spring ResourceLoader to use for loading Rythm template files.
	 * The default is DefaultResourceLoader. Will get overridden by the
	 * ApplicationContext if running in a context.
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.context.ApplicationContext
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
	 * Return the Spring ResourceLoader to use for loading Rythm template files.
	 */
	protected ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public void setDevMode(boolean mode) {
	    this.devMode = mode;
	}

    public void setDevModeSensor(String devModeSensor) throws Exception {
            Class<DevModeSensor> c = (Class<DevModeSensor>)Class.forName(devModeSensor);
            this.devModeSensor = c.newInstance();
    }

    protected DevModeSensor getDevModeSensor() {
        if (null == devModeSensor) {
            devModeSensor = new DevModeSensor.DefaultDevModeSensor(getApplicationContext());
        }
        return devModeSensor;
    }

	protected boolean isDevMode() {
	    return this.devMode;
	}

    /**
     * Sub class to add more rythm configuration
     * @param config
     */
	protected void configRythm(Map<String, Object> config) {
	}

	private boolean getDevModeFromDevModeSensor() {
        boolean mode = getDevModeSensor().isDevMode();
        setDevMode(mode);
        return mode;
    }

	/**
	 * Prepare the RythmEngine instance and return it.
	 * @return the RythmEngine instance
	 * @throws java.io.IOException if the config file wasn't found
	 * @throws RythmException on Rythm initialization failure
	 */
	public RythmEngine createRythmEngine() throws IOException, RythmException {
		Map<String, Object> p = new HashMap<String, Object>();

        p.put(RythmConfigurationKey.ENGINE_PLUGIN_VERSION.getKey(), Version.VALUE);

		// Load config file if set.
		if (this.configLocation != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Loading Rythm config from [" + this.configLocation + "]");
			}
			CollectionUtils.mergePropertiesIntoMap(PropertiesLoaderUtils.loadProperties(this.configLocation), p);
		}


		// Merge local properties if set.
		if (!this.engineConfig.isEmpty()) {
			p.putAll(this.engineConfig);
		}

		// Set dev mode
        if (null == devMode) {
            // check if devMode is set in engineConfig
            String k = RythmConfigurationKey.ENGINE_MODE.getKey();
            if (p.containsKey(k)) {
                String s = p.get(k).toString();
                devMode = Rythm.Mode.dev.name().equalsIgnoreCase(s);
            } else {
                devMode = getDevModeFromDevModeSensor();
            }
        }
        Rythm.Mode mode = devMode ? Rythm.Mode.dev : Rythm.Mode.prod;
        p.put(RythmConfigurationKey.ENGINE_MODE.getKey(), mode);

        // Cache
        p.put(RythmConfigurationKey.CACHE_ENABLED.getKey(), enableCache);

        // the i18 message resolver
        SpringI18nMessageResolver i18n = new SpringI18nMessageResolver();
        i18n.setApplicationContext(getApplicationContext());
        p.put(RythmConfigurationKey.I18N_MESSAGE_RESOLVER.getKey(), i18n);

		// Set a resource loader path, if required.
        List<ITemplateResourceLoader> loaders = null;
		if (this.resourceLoaderPath != null) {
            loaders = new ArrayList<ITemplateResourceLoader>();
            String[] paths = StringUtils.commaDelimitedListToStringArray(resourceLoaderPath);
            for (String path : paths) {
                loaders.add(new SpringResourceLoader(path, resourceLoader));
            }
            p.put(RythmConfigurationKey.RESOURCE_LOADER_IMPLS.getKey(), loaders);
            p.put(RythmConfigurationKey.RESOURCE_DEF_LOADER_ENABLED.getKey(), false);
        }

        configRythm(p);

		// Apply properties to RythmEngine.
        RythmEngine engine = newRythmEngine(p);

        if (null != loaders) {
            for (ITemplateResourceLoader loader : loaders) {
                loader.setEngine(engine);
            }
        }

		postProcessRythmEngine(engine);

		return engine;
	}

	/**
	 * Return a new RythmEngine. Subclasses can override this for
	 * custom initialization, or for using a mock object for testing.
	 * <p>Called by {@code createRythmEngine()}.
	 * @return the RythmEngine instance
	 * @throws IOException if a config file wasn't found
	 * @throws RythmException on Rythm initialization failure
	 * @see #createRythmEngine()
	 */
	protected RythmEngine newRythmEngine(Map<String, ?> conf) throws IOException, RythmException {
		return new RythmEngine(conf);
	}

	/**
	 * To be implemented by subclasses that want to to perform custom
	 * post-processing of the RythmEngine after this FactoryBean
	 * performed its default configuration (but before RythmEngine.init).
	 * <p>Called by {@code createRythmEngine()}.
	 * @param RythmEngine the current RythmEngine
	 * @throws IOException if a config file wasn't found
	 * @throws RythmException on Rythm initialization failure
	 * @see #createRythmEngine()
	 */
	protected void postProcessRythmEngine(RythmEngine RythmEngine)
			throws IOException, RythmException {
	}

}
