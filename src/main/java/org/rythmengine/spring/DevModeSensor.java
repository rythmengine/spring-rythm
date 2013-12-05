package org.rythmengine.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Created by luog on 6/12/13.
 */
public interface DevModeSensor {
    boolean isDevMode();

    /**
     * A default dev mode sensor that check if a file named "dev.mode" exists in the project folder
     */
    public static class DefaultDevModeSensor implements DevModeSensor {

        public static final String TAG_FILE = "dev.mode";

        private ApplicationContext ctx;

        public DefaultDevModeSensor(ApplicationContext ctx) {
            Assert.notNull(ctx);
            this.ctx = ctx;
        }

        @Override
        public boolean isDevMode() {
            Resource r = ctx.getResource(TAG_FILE);
            return null != r && r.exists();
        }
    }
}
