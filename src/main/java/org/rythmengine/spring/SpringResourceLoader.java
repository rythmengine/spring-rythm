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

import org.osgl.util.S;
import org.rythmengine.resource.ITemplateResource;
import org.rythmengine.resource.ResourceLoaderBase;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Rythm ResourceLoader adapter that loads via a Spring ResourceLoader.
 * Used by RythmResourceFactory for any resource loader path that cannot
 * be resolved to a {@code java.io.File}.
 * <p/>
 * <p>Note that this loader does not allow for modification detection:
 * Use Rythm's default FileResourceLoader for {@code java.io.File}
 * resources.
 * <p/>
 * <p>Expects "spring.resource.loader" and "spring.resource.loader.path"
 * application attributes in the Rythm runtime: the former of type
 * {@code org.springframework.core.io.ResourceLoader}, the latter a String.
 *
 * @author Gelin Luo
 * @see RythmEngineFactory#setResourceLoaderPath
 * @see org.springframework.core.io.ResourceLoader
 * @see org.rythmengine.resource.FileResourceLoader
 * @since 14.03.2004
 */
class SpringResourceLoader extends ResourceLoaderBase {

   	private org.springframework.core.io.ResourceLoader springRsrcLoader;

   	private String root;

    SpringResourceLoader(String root, org.springframework.core.io.ResourceLoader springRsrcLoader) {
        if (null == springRsrcLoader) {
            throw new NullPointerException();
        }
        if (S.blank(root)) {
            throw new IllegalArgumentException("root cannot be empty or blank string");
        }
        this.springRsrcLoader = springRsrcLoader;
        if (root.endsWith("/")) {
            root = root.substring(0, root.length() - 1);
        }
        this.root = root;
    }

    @Override
    public String getResourceLoaderRoot() {
        return root;
    }

    @Override
    public ITemplateResource load(String path) {
        if (!path.startsWith(root) && !path.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
            StringBuilder sb = S.builder(root);
            if (path.startsWith("/")) sb.append(path);
            else sb.append("/").append(path);
            path = sb.toString();
        }
        Resource r = springRsrcLoader.getResource(path);
        if (null == r || !r.isReadable()) {
            if (path.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX)) {
                return null;
            }
            // try classpath
            path = ResourceLoader.CLASSPATH_URL_PREFIX + path;
            return load(path);
        }
        return new SpringTemplateResource(r, this);
    }
}
