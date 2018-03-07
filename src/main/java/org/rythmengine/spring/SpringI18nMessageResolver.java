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
import org.rythmengine.extension.II18nMessageResolver;
import org.rythmengine.template.ITemplate;
import org.rythmengine.utils.S;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 2/12/13
 * Time: 12:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpringI18nMessageResolver implements II18nMessageResolver, ApplicationContextAware {

    private ApplicationContext msgSrc;

    @Override
    public String getMessage(ITemplate template, String key, Object... args) {
        Locale locale = null;
        if (args.length > 0) {
            Object arg0 = args[0];
            if (arg0 instanceof Locale) {
                locale = (Locale)arg0;
                Object[] args0 = new Object[args.length - 1];
                System.arraycopy(args, 1, args0, 0, args.length - 1);
                args = args0;
            }
            for (int i = 0; i < args.length; ++i) {
                Object arg = args[i];
                if (arg instanceof String) {
                    arg = S.i18n(template, (String) arg);
                }
                args[i] = arg;
            }
        }
        if (null == locale && null != template) {
            locale = null == template ? RythmEngine.get().renderSettings.locale() : template.__curLocale();
        }
        try {
            return msgSrc.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (null == applicationContext) {
            throw new NullPointerException();
        }
        msgSrc = applicationContext;
    }
}
