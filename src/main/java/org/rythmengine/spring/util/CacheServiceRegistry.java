package org.rythmengine.spring.util;

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

import org.osgl.cache.CacheService;
import org.osgl.logging.L;
import org.osgl.logging.Logger;
import org.osgl.util.C;
import org.osgl.util.E;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * Created by luog on 16/03/14.
 */
@Component
public class CacheServiceRegistry implements DisposableBean {

    protected static Logger logger = L.get(CacheServiceRegistry.class);

    private static C.List<CacheService> registry = C.newList();

    public static void register(CacheService svc) {
        E.NPE(svc);
        registry.add(svc);
    }

    @Override
    public void destroy() throws Exception {
        _destroy();
    }

    public static void _destroy() {
        for (CacheService svc : registry) {
            try {
                svc.shutdown();
            } catch (Exception e) {
                logger.warn(e, "Error shutting down cache service: %s", svc);
                // ignore
            }
        }
        registry.clear();
    }
}
