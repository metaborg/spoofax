package org.strategoxt.imp.metatooling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class PluginClasspathProvider implements IAntPropertyValueProvider {
    public String getAntPropertyValue(String antPropertyName) {

        final StringBuilder classpathBuilder = new StringBuilder();
        boolean first = true;
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final Bundle[] bundles = bundleContext.getBundles();
        for (final Bundle bundle : bundles) {
            try {
                if (!first) {
                    classpathBuilder.append(":");
                }
                first = false;

                final File file = FileLocator.getBundleFile(bundle);
                final String path = file.getAbsolutePath();
                if (path.endsWith(".jar")) {
                    classpathBuilder.append(path);
                    System.out.println(path);
                } else {
                    final File classPath = Paths.get(path, "target", "classes").toFile();
                    if (classPath.exists()) {
                        classpathBuilder.append(classPath);
                        System.out.println(classPath);
                    } else {
                        classpathBuilder.append(path);
                        System.out.println(path);
                    }
                }
            } catch (IOException e) {

            }
        }

        System.out.println();
        System.out.println();
        System.out.println();

        return classpathBuilder.toString();
    }
}
