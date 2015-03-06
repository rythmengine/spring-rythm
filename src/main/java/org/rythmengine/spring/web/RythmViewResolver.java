package org.rythmengine.spring.web;

import org.osgl.util.S;
import org.springframework.web.servlet.view.AbstractTemplateViewResolver;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

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

    @Override
    protected AbstractUrlBasedView buildView(String viewName) throws Exception {
        if (S.blank(viewName)) {
            return RythmView.EMPTY_VIEW;
        } else {
            return super.buildView(viewName);
        }
    }
}
