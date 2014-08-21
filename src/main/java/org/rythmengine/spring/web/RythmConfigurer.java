package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;
import org.rythmengine.conf.RythmConfigurationKey;
import org.rythmengine.exception.RythmException;
import org.rythmengine.extension.ISourceCodeEnhancer;
import org.rythmengine.spring.RythmEngineFactory;
import org.rythmengine.spring.util.CacheServiceRegistry;
import org.rythmengine.template.ITemplate;
import org.rythmengine.utils.S;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 2/12/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
@EnableWebMvc
@ComponentScan("org.rythmengine.spring.web")
public class RythmConfigurer extends RythmEngineFactory implements
        RythmHolder, InitializingBean, DisposableBean,
        ResourceLoaderAware, ServletContextAware,
        WebMvcConfigurer {

    /**
     * so that app developer can retrieve the servlet context with
     * {@code @servletContext}
     */
    public static final String CTX_SERVLET_CONTEXT = "servletContext";

    //public static final String CTX_

    public static final String CONF_USER_CONTEXT = "userContext";
    /**
     * Do we allow add all http request parameters into the render args?
     */
    public static final String CONF_OUTOUT_REQ_PARAMS = "outputRequestParameters";

    public static final String CONF_UNDERSCORE_IMPLICIT_VAR_NAME = "underscoreImplicityVarName";

    public static final String CONF_ENABLE_SESSION_MANAGER = "enableSessionManager";

    public static final String CONF_ENABLE_USER_AGENT_DETECTOR = "enableUserAgentDetector";

    public static final String CONF_CSRF_PARAM_NAME = "csrfParameterName";

    public static final String CONF_CSRF_HEADER_NAME = "csrfHeaderName";

    private static RythmEngine engine;

    private ServletContext ctx;

    private boolean outputRequestParameters = false;

    private boolean underscoreImplicitVariableName = false;

    private String sessionCookiePrefix = null;

    private String sessionCookieExpire = null;

    private String pingPath = null;

    private boolean sessionCookieSecure = false;

    private boolean transientSessionCookie = true;

    private boolean autoCsrfCheck = true;

    private boolean enableSessionManager = false;

    private boolean enableUserAgentDetector = false;

    private boolean enableCacheFor = false;

    private static String secretKey = null;

    private static SecretKeySensor secretKeySensor;

    private String csrfParamName = null;

    private String csrfHeaderName = null;

    private String autoImports = null;

    boolean customErrorPages = false;

    private static RythmConfigurer inst;

    public static RythmConfigurer getInstance() {
        return inst;
    }

    public void setRythmEngine(RythmEngine engine) {
        this.engine = engine;
    }

    @Override
    public RythmEngine getRythmEngine() {
        return engine;
    }

    public static RythmEngine engine() {
        return engine;
    }

    public void setOutputRequestParameters(boolean outputRequestParameters) {
        this.outputRequestParameters = outputRequestParameters;
    }

    public void setUnderscoreImplicitVariableName(boolean underscoreImplicitVariableName) {
        this.underscoreImplicitVariableName = underscoreImplicitVariableName;
    }

    public void setAutoCsrfCheck(boolean autoCsrfCheck) {
        this.autoCsrfCheck = autoCsrfCheck;
    }

    public void setAutoImports(String autoImports) {
        this.autoImports = autoImports;
    }

    public void setEnableCacheFor(boolean enableCacheFor) {
        this.enableCacheFor = enableCacheFor;
    }

    public void setEnableSessionManager(boolean enableSessionManager) {
        this.enableSessionManager = enableSessionManager;
    }

    public void setEnableUserAgentDetector(boolean enableUserAgentDetector) {
        this.enableUserAgentDetector = enableUserAgentDetector;
    }

    public boolean sessionManagerEnabled() {
        return enableSessionManager;
    }

    public void setSecretKeySensor(String secretKeySensor) throws Exception {
        Class<SecretKeySensor> c = (Class<SecretKeySensor>) Class.forName(secretKeySensor);
        this.secretKeySensor = c.newInstance();
    }

    private SecretKeySensor getSecretKeySensor() {
        if (null == secretKeySensor) {
            secretKeySensor = new SecretKeySensor.DefaultSecretKeySensor(getApplicationContext());
        }
        return secretKeySensor;
    }

    public void setSecretKey(String secretKey) {
        Assert.hasText(secretKey);
        int len = secretKey.length();
        int delta = 16 - len;
        if (delta > 0) {
            StringBuilder sb = new StringBuilder(secretKey);
            for (int i = 0; i < delta; ++i) {
                sb.append("\u0000");
            }
            secretKey = sb.toString();
        }
        RythmConfigurer.secretKey = secretKey;
    }

    public static synchronized String getSecretKey() {
        if (null == secretKey && null != secretKeySensor) {
            secretKey = secretKeySensor.getSecretKey();
        }
        return secretKey;
    }

    public void setSessionCookiePrefix(String sessionCookiePrefix) {
        Assert.notNull(sessionCookiePrefix);
        this.sessionCookiePrefix = sessionCookiePrefix;
    }

    public void setSessionCookieExpire(String sessionCookieExpire) {
        Assert.notNull(sessionCookieExpire);
        this.sessionCookieExpire = sessionCookieExpire;
    }

    public void setPingPath(String pingPath) {
        this.pingPath = pingPath;
    }

    public void setTransientSessionCookie(boolean value) {
        this.transientSessionCookie = value;
    }

    public void setCustomErrorPages(boolean customErrorPages) {
        this.customErrorPages = customErrorPages;
    }

    public void setCsrfParamName(String csrfParamName) {
        Assert.notNull(csrfParamName);
        this.csrfParamName = csrfParamName;
    }

    public void setCsrfHeaderName(String csrfHeaderName) {
        Assert.notNull(csrfHeaderName);
        this.csrfHeaderName = csrfHeaderName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == engine) {
            engine = createRythmEngine();
        }
        inst = this;
        if (null == secretKey) {
            secretKey = getSecretKeySensor().getSecretKey();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != engine) {
            engine.shutdown();
            CacheServiceRegistry._destroy();
        }
    }

    private Map<String, Object> userContext = new HashMap<String, Object>();

    private void setUserContext(String key, Object v) {
        userContext.put(key, v);
    }

    public static ISourceCodeEnhancer sourceCodeEnhancer(final String autoImports,
                                                         final boolean underscoreImplicitVariableName) {
        return new ISourceCodeEnhancer() {
            ImplicitVariables implicitVariables = new ImplicitVariables(underscoreImplicitVariableName);

            @Override
            public List<String> imports() {
                List<String> l = new ArrayList<String>();
                if (null != autoImports) {
                    return Arrays.asList(autoImports.split("[, ;:]+"));
                }
                return l;
            }

            @Override
            public String sourceCode() {
                return "protected org.rythmengine.utils.RawData url(String path) {\n" +
                        "\treturn new org.rythmengine.utils.RawData(org.rythmengine.spring.web.util.ControllerUtil.url(path, __request));\n" +
                        "}\n\n" +
                        "protected org.rythmengine.utils.RawData fullUrl(String path) {\n" +
                        "\treturn new org.rythmengine.utils.RawData(org.rythmengine.spring.web.util.ControllerUtil.fullUrl(path, __request));\n" +
                        "}\n";
            }

            @Override
            public Map<String, ?> getRenderArgDescriptions() {
                Map<String, Object> m = new HashMap<String, Object>();
                for (ImplicitVariables.Var var : implicitVariables.vars) {
                    m.put(var.name(), var.type);
                }
                return m;
            }

            @Override
            public void setRenderArgs(ITemplate template) {
                // already set in RythmView
                //                Map<String, Object> m = new HashMap<String, Object>();
                //                for (ImplicitVariables.Var var : implicitVariables.vars) {
                //                    m.put(var.name(), var.evaluate());
                //                }
                //                template.__setRenderArgs(m);
            }
        };
    }

    @Override
    protected void configRythm(Map<String, Object> config) {
        WebApplicationContext ctx = (WebApplicationContext) getApplicationContext();
        boolean allowFileWrite = true;
        Object o = config.get(RythmConfigurationKey.ENGINE_FILE_WRITE_ENABLED.getKey());
        if (null != o) {
            allowFileWrite = Boolean.parseBoolean(String.valueOf(o));
        }
        if (allowFileWrite && !config.containsKey(RythmConfigurationKey.HOME_TMP.getKey())) {
            File tmpdir;
            if (null != ctx) {
                tmpdir = (File) ctx.getServletContext().getAttribute("javax.servlet.context.tempdir");
            } else {
                tmpdir = new File(System.getProperty("java.io.tmpdir"));
            }
            if (null != tmpdir) {
                tmpdir = new File(tmpdir, "__rythm");
                if (!tmpdir.exists()) {
                    tmpdir.mkdirs();
                }
                config.put(RythmConfigurationKey.HOME_TMP.getKey(), tmpdir);
            }
        }

        config.put(RythmConfigurationKey.CODEGEN_SOURCE_CODE_ENHANCER.getKey(), sourceCodeEnhancer(autoImports, underscoreImplicitVariableName));
    }

    @Override
    protected void postProcessRythmEngine(RythmEngine engine) throws IOException, RythmException {
        engine.setProperty(CONF_USER_CONTEXT, userContext);
        setUserContext(CTX_SERVLET_CONTEXT, ctx);
        setUserContext(ServletContext.class.getName(), ctx);
        engine.setProperty(CONF_OUTOUT_REQ_PARAMS, outputRequestParameters);
        engine.setProperty(CONF_UNDERSCORE_IMPLICIT_VAR_NAME, underscoreImplicitVariableName);
        engine.setProperty(CONF_ENABLE_SESSION_MANAGER, enableSessionManager);
        engine.setProperty(CONF_ENABLE_USER_AGENT_DETECTOR, enableUserAgentDetector);
        if (null != csrfHeaderName) engine.setProperty(CONF_CSRF_HEADER_NAME, csrfHeaderName);
        if (null != csrfParamName) engine.setProperty(CONF_CSRF_PARAM_NAME, csrfParamName);
        sessionCookieSecure = engine.isProdMode();
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        ctx = servletContext;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {

    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public Validator getValidator() {
        return null;
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {

    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

    }

    @Override
    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {

    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (enableSessionManager) {
            if (S.empty(getSecretKey())) {
                throw new RuntimeException("No secure salt configured while session manager is enabled");
            }
            SessionManager sm = new SessionManager();
            sm.setSessionExpire(sessionCookieExpire);
            sm.setSessionPrefix(sessionCookiePrefix);
            sm.setCookieSecure(sessionCookieSecure);
            sm.setNoPersistentCookie(transientSessionCookie);
            sm.setPingPath(pingPath);
            registry.addInterceptor(sm);
        }
        if (autoCsrfCheck) {
            CsrfManager csrfManager = new CsrfManager();
            csrfManager.setParameterName(csrfParamName);
            csrfManager.setHeaderName(csrfHeaderName);
            registry.addInterceptor(csrfManager);
        }
        if (enableCacheFor && enableCache) {
            CacheInterceptor ci = new CacheInterceptor();
            ci.setEngine(engine);
            registry.addInterceptor(ci);
        }
        if (enableUserAgentDetector) {
            UADetector ua = new UADetector();
            registry.addInterceptor(ua);
        }
    }

    @Override
    public MessageCodesResolver getMessageCodesResolver() {
        return null;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {

    }
}
