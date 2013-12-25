package org.rythmengine.spring.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rythmengine.utils.IO;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.InputStream;

/**
 * Created by luog on 6/12/13.
 */
public interface SecretKeySensor {
    String getSecretKey();

    /**
     * A default secret key sensor that check if a file named "secret.key" exists in the project folder
     */
    public static class DefaultSecretKeySensor implements SecretKeySensor {

        protected final Log logger = LogFactory.getLog(getClass());

        public static final String TAG_FILE = "secret.key";

        private ApplicationContext ctx;

        public DefaultSecretKeySensor(ApplicationContext ctx) {
            Assert.notNull(ctx);
            this.ctx = ctx;
        }

        @Override
        public String getSecretKey() {
            Resource r = ctx.getResource(TAG_FILE);
            if (null == r || !r.exists() || !r.isReadable()) {
                return null;
            }

            InputStream is;
            try {
                is = r.getInputStream();
                String s = IO.readContentAsString(is);
                String[] sa = s.split("\\s");
                return sa[0];
            } catch (Exception e) {
                // ignore
                logger.warn("Error reading secret key file", e);
                return null;
            }
        }
    }
}
