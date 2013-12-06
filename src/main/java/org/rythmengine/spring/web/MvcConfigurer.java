package org.rythmengine.spring.web;

import org.rythmengine.RythmEngine;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by luog on 5/12/13.
 */
@Configuration
@EnableWebMvc
public class MvcConfigurer extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext ctx;

    @Autowired
    private RythmConfigurer rythmConfigurer;

    @Autowired
    public MvcConfigurer() {
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RythmEngine engine = rythmConfigurer.getRythmEngine();
        if (engine.conf().cacheDisabled()) return;
        CacheHandlerInterceptor hi = new CacheHandlerInterceptor();
        hi.setApplicationContext(ctx);
        hi.setEngine(engine);
        registry.addInterceptor(hi);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }
}
