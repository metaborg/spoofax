package org.strategoxt.imp.metatooling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Ant property provider that generates a classpath from all installed plugins, to be used for Java compilation in a
 * Spoofax ant build.
 */
public class PluginClasspathProvider implements IAntPropertyValueProvider {
    public String getAntPropertyValue(String antPropertyName) {
        final StringBuilder classpathBuilder = new StringBuilder();
        boolean first = true;
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final Bundle[] bundles = bundleContext.getBundles();
        for (final Bundle bundle : bundles) {
            try {
                if (!first) {
                    classpathBuilder.append(File.pathSeparator);
                }
                first = false;

                final File file = FileLocator.getBundleFile(bundle);
                final String path = file.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    /*
                     * An installed JAR plugin.
                     */
                    classpathBuilder.append(path);
                } else {
                    final File targetClasses = Paths.get(path, "target", "classes").toFile();
                    if (targetClasses.exists()) {
                        /*
                         * A plugin under development. Plugins under development have a target/classes directory with
                         * all their classes.
                         */
                        classpathBuilder.append(targetClasses);
                    } else {
                        /*
                         * An installed unpacked plugin. Class files are extracted in this directory.
                         */
                        classpathBuilder.append(path);
                    }
                }
            } catch (IOException e) {

            }
        }

        return classpathBuilder.toString();
    }
}
