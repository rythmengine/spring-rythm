package org.rythmengine.spring;

import org.rythmengine.RythmEngine;
import org.rythmengine.spring.web.RythmHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.Phased;
import org.springframework.util.Assert;

/**
 * Created by luog on 10/12/13.
 */
public class TransformerBean implements InitializingBean, Phased, ApplicationContextAware {
    private ApplicationContext appCtx;
    private RythmEngine engine;

    public void setRythmEngine(RythmEngine engine) {
        Assert.notNull(engine);
        this.engine = engine;
    }

    private RythmEngine getRythmEngine() {
        if (null == engine) {
            engine = autodetectRythmEngine();
        }
        return engine;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RythmEngine engine = getRythmEngine();
        engine.registerTransformer(getClass());
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

    private ApplicationContext getApplicationContext() {return appCtx;}

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Assert.notNull(applicationContext);
        appCtx = applicationContext;
    }

    @Override
    public int getPhase() {
        return 5;
    }
}
