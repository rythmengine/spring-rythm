package org.rythmengine.spring;

import org.rythmengine.resource.ITemplateResource;
import org.rythmengine.resource.ResourceLoaderBase;
import org.rythmengine.utils.S;
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
        if (S.empty(root)) {
            throw new IllegalArgumentException("root cannot be empty");
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
            path = root  + "/" + path;
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