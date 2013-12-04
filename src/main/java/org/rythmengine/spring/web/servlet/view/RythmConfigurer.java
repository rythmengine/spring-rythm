package org.rythmengine.spring.web.servlet.view;

import org.rythmengine.RythmEngine;
import org.rythmengine.conf.RythmConfigurationKey;
import org.rythmengine.exception.RythmException;
import org.rythmengine.extension.ISourceCodeEnhancer;
import org.rythmengine.spring.ui.RythmEngineFactory;
import org.rythmengine.template.ITemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 2/12/13
 * Time: 7:58 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
@ComponentScan("org.rythmengine.spring.web.servlet.view")
public class RythmConfigurer extends RythmEngineFactory implements RythmConfig, InitializingBean, ResourceLoaderAware, ServletContextAware {

    /**
     * so that app developer can retrieve the servlet context with
     * {@code @servletContext}
     */
    public static final String CTX_SERVLET_CONTEXT = "servletContext";

    public static final String PROP_USER_CONTEXT = "userContext";

    /**
     * Do we allow add all http request parameters into the render args?
     */
    public static final String CONF_OUTOUT_REQ_PARAMS = "outputRequestParameters";

    public static final String CONF_UNDERSCORE_IMPLICIT_VAR_NAME = "underscoreImplicityVarName";

    private RythmEngine engine;

    private ServletContext ctx;

    private boolean outputRequestParameters = false;

    private boolean underscoreImplicitVariableName = false;

    public void setRythmEngine(RythmEngine engine) {
        this.engine = engine;
    }

    @Override
    public RythmEngine getRythmEngine() {
        return engine;
    }

    public void setOutputRequestParameters(boolean outputRequestParameters) {
        this.outputRequestParameters = outputRequestParameters;
    }

    public void setUnderscoreImplicitVariableName(boolean underscoreImplicitVariableName) {
        this.underscoreImplicitVariableName = underscoreImplicitVariableName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == engine) {
            engine = createRythmEngine();
        }
    }

    private Map<String, Object> userContext = new HashMap<String, Object>();

    private void setUserContext(String key, Object v) {
        userContext.put(key, v);
    }

    @Override
    protected void configRythm(Map<String, Object> config) {
        config.put(RythmConfigurationKey.CODEGEN_SOURCE_CODE_ENHANCER.getKey(), new ISourceCodeEnhancer(){
            ImplicitVariables implicitVariables = new ImplicitVariables(underscoreImplicitVariableName);
            @Override
            public List<String> imports() {
                List<String> l = new ArrayList<String>();
                return l;
            }

            @Override
            public String sourceCode() {
                return "";
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
                Map<String, Object> m = new HashMap<String, Object>();
                for (ImplicitVariables.Var var : implicitVariables.vars) {
                    m.put(var.name(), var.evaluate());
                }
                template.__setRenderArgs(m);
            }
        });
    }

    @Override
    protected void postProcessRythmEngine(RythmEngine engine) throws IOException, RythmException {
        engine.setProperty(PROP_USER_CONTEXT, userContext);
        setUserContext(CTX_SERVLET_CONTEXT, ctx);
        setUserContext(ServletContext.class.getName(), ctx);
        engine.setProperty(CONF_OUTOUT_REQ_PARAMS, outputRequestParameters);
        engine.setProperty(CONF_UNDERSCORE_IMPLICIT_VAR_NAME, underscoreImplicitVariableName);
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        ctx = servletContext;
    }
}
