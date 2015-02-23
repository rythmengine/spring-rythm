package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;
import org.rythmengine.exception.RythmException;
import org.rythmengine.extension.ICodeType;
import org.rythmengine.internal.compiler.TemplateClass;
import org.rythmengine.resource.ITemplateResource;
import org.rythmengine.resource.TemplateResourceManager;
import org.rythmengine.template.TemplateBase;
import org.rythmengine.utils.IO;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.view.AbstractTemplateView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 2/12/13
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RythmView extends AbstractTemplateView {

    private RythmEngine engine;

    private ITemplateResource rsrc;

    private TemplateClass tc;

    private ICodeType codeType;

    private boolean outputReqParams = false;

    private boolean underscoreImplicitVarNames = false;

    private boolean enableSessionManager = false;

    private boolean enableUserAgentDetector = false;

    public void setRythmEngine(RythmEngine engine) {
        this.engine = engine;
    }

    protected RythmEngine getRythmEngine() {
        return this.engine;
    }

    @Override
    protected void initApplicationContext(ApplicationContext context) {
        super.initApplicationContext(context);
        RythmEngine engine = getRythmEngine();
        if (engine == null) {
            engine = autodetectRythmEngine();
            // No explicit RythmEngine: try to autodetect one.
            setRythmEngine(engine);
        }
        Object o = engine.getProperty(RythmConfigurer.CONF_OUTOUT_REQ_PARAMS);
        if (null != o) {
            try {
                outputReqParams = (Boolean) o;
            } catch (Exception e) {
                // ignore it
                logger.warn("error set output request parameter config", e);
            }
        }
        o = engine.getProperty(RythmConfigurer.CONF_UNDERSCORE_IMPLICIT_VAR_NAME);
        if (null != o) {
            try {
                underscoreImplicitVarNames = (Boolean) o;
            } catch (Exception e) {
                // ignore it
                logger.warn("error set underscore implicit variable name config", e);
            }
        }
        o = engine.getProperty(RythmConfigurer.CONF_ENABLE_SESSION_MANAGER);
        if (null != o) {
            try {
                enableSessionManager = (Boolean) o;
            } catch (Exception e) {
                // ignore it
                logger.warn("error set enable session manager config", e);
            }
        }

        o = engine.getProperty(RythmConfigurer.CONF_ENABLE_USER_AGENT_DETECTOR);
        if (null != o) {
            try {
                enableUserAgentDetector = (Boolean)o;
            } catch (Exception e) {
                logger.warn("error set enable user agent detector config", e);
            }
        }

        String url = getUrl();
        TemplateResourceManager rm = engine.resourceManager();
        rsrc = rm.getResource(url);
        if (null == rsrc || !rsrc.isValid()) {
            // try guess it is ".html" file
            rsrc = rm.getResource(url + ".html");
        }
    }

    protected RythmEngine autodetectRythmEngine() throws BeansException {
        try {
            RythmHolder rythmHolder = BeanFactoryUtils.beanOfTypeIncludingAncestors(
                    getApplicationContext(), RythmHolder.class, true, false);
            return rythmHolder.getRythmEngine();
        } catch (NoSuchBeanDefinitionException ex) {
            throw new ApplicationContextException(
                    "Must define a single RythmHolder bean in this web application context " +
                            "(may be inherited): RythmConfigurer is the usual implementation. " +
                            "This bean may be given any name.", ex);
        }
    }

    private RythmException re;

    @Override
    public boolean checkResource(Locale locale) throws Exception {
        if (null != tc && tc.isDefinable()) {
            return true;
        }
        if (!rsrc.isValid()) {
            return false;
        }
        try {
            tc = engine.getTemplateClass(rsrc);
            String fullName = tc.getTagName();
            engine.registerTemplate(fullName, tc.asTemplate(engine));
            codeType = rsrc.codeType(engine);
            re = null;
            return true;
        } catch (RythmException e) {
            if (engine.isDevMode()) {
                re = e;
                return true;
            }
            throw new ApplicationContextException(
                    "Failed to load rythm template for URL [" + getUrl() + "]", e);
        }
    }

    static final ThreadLocal<Map<String, Object>> renderArgs = new ThreadLocal<Map<String, Object>>();

    private void prepareImplicitArgs(Map<String, Object> params, HttpServletRequest request, HttpServletResponse response) {
        boolean u = underscoreImplicitVarNames;
        params.put(u ? "_request" : "request", request);
        params.put("__request", request);
        params.put(u ? "_response" : "response", response);
        HttpSession httpSession = request.getSession(false);
        params.put(u ? "_httpSession" : "httpSession", httpSession);
        if (enableSessionManager) {
            params.put(u ? "_session" : "session", Session.current());
            params.put(u ? "_flash" : "flash", Flash.current());
        }
        if (enableUserAgentDetector) {
            params.put(u ? "_userAgent" : "userAgent", UADetector.get());
        }

        String csrfHeaderName = engine.getProperty(RythmConfigurer.CONF_CSRF_HEADER_NAME);
        String csrfParamName = engine.getProperty(RythmConfigurer.CONF_CSRF_PARAM_NAME);
        Csrf csrf = null != Session.current() ?
                new Csrf(csrfParamName, csrfHeaderName) :
                new Csrf(csrfParamName, csrfHeaderName, request.getSession());
        params.put(u ? "_csrf" : "csrf", csrf);
    }

    @Override
    protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RythmEngine engine = this.engine;
        Map<String, Object> params = new HashMap<String, Object>();
        if (null != re) {
            checkResource(null);
            if (null != re) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                prepareImplicitArgs(params, request, response);
                params.put("exception", re);
                engine.render(response.getOutputStream(), "errors/500.html", params);
                return;
            }
        }

        Locale locale = LocaleContextHolder.getLocale();
        engine = engine.prepare(locale);
        try {
            TemplateClass tc = this.tc;
            if (engine.mode().isDev()) {
                tc = engine.getTemplateClass(rsrc);
            }
            TemplateBase t = (TemplateBase) tc.asTemplate(engine);
            if (outputReqParams) {
                Map reqMap = request.getParameterMap();
                for (Object o : reqMap.keySet()) {
                    String k = o.toString();
                    String[] va = request.getParameterValues(k);
                    if (va.length == 1) {
                        params.put(k, va[0]);
                    } else if (va.length > 1) {
                        params.put(k, va);
                    }
                }
            }
            prepareImplicitArgs(params, request, response);
            params.putAll(model);
            //renderArgs.set(params);
            t.__setRenderArgs(params);
            // TODO fix this: doesn't work when extends is taking place
            // t.render(response.getOutputStream());
            String s = t.render();
            response.setCharacterEncoding("utf-8");
            IO.writeContent(s, response.getWriter());
            CacheInterceptor.KeyAndTTL kt = CacheInterceptor.currentCacheKey();
            if (null != kt) {
                engine.cache(kt.key, s, kt.ttl);
            }
        } catch (RythmException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            if (engine.isDevMode()) {
                engine.render(response.getOutputStream(), "errors/500.html", e);
            } else {
                RythmExceptionHandler.getInternalServerErrorVisitor().visit(e);
                Map<String, Object> args = new HashMap<String, Object>();
                args.put("message", "Internal server error");
                args.put("attachment", e);
                String s = engine.render("errors/prod/500.html", args);
                IO.writeContent(s, response.getWriter());
            }
        } finally {
            RythmEngine.renderCleanUp();
        }
    }

    static final AbstractTemplateView EMPTY_VIEW = new AbstractTemplateView() {
        @Override
        protected void renderMergedTemplateModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
            IO.close(response.getWriter());
        }

        @Override
        public void afterPropertiesSet() throws Exception {
        }
    };
}
