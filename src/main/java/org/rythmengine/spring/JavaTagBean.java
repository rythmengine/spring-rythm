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
import org.rythmengine.spring.web.RythmHolder;
import org.rythmengine.template.JavaTagBase;
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
public abstract class JavaTagBean extends JavaTagBase implements InitializingBean, Phased, ApplicationContextAware {

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
        engine.registerFastTag(this);
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

    abstract public String __getName();
}
