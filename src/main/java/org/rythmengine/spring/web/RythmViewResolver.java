package org.rythmengine.spring.web;

import org.springframework.web.servlet.view.AbstractTemplateViewResolver;

/**
 * Created with IntelliJ IDEA.
 * User: luog
 * Date: 2/12/13
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class RythmViewResolver extends AbstractTemplateViewResolver {

    public RythmViewResolver() {
   		setViewClass(requiredViewClass());
   	}

   	/**
   	 * Requires {@link RythmView}.
   	 */
   	@Override
   	protected Class requiredViewClass() {
   		return RythmView.class;
   	}

}
