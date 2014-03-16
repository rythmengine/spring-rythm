package org.rythmengine.spring.util;

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
