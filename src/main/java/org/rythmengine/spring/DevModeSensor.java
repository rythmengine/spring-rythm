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
