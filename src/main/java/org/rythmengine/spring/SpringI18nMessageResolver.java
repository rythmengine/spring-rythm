package org.rythmengine.spring;

import org.rythmengine.RythmEngine;
import org.rythmengine.extension.II18nMessageResolver;
import org.rythmengine.template.ITemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
        }
        if (null == locale && null != template) {
            locale = null == template ? RythmEngine.get().renderSettings.locale() : template.__curLocale();
        }
        return msgSrc.getMessage(key, args, locale);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (null == applicationContext) {
            throw new NullPointerException();
        }
        msgSrc = applicationContext;
    }
}
