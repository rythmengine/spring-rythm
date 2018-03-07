package org.rythmengine.spring.web;

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

import org.osgl.logging.L;
import org.osgl.logging.Logger;
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

        protected final Logger logger = L.get(getClass());

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
