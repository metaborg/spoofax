package org.strategoxt.imp.metatooling;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Ant property provider that generates a classpath from all installed plugins, to be used for Java
 * compilation in a Spoofax ant build.
 */
public class PluginClasspathProvider implements IAntPropertyValueProvider {
    public String getAntPropertyValue(String antPropertyName) {
        final Collection<String> classpath = Lists.newArrayList();
        final BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final Bundle[] bundles = bundleContext.getBundles();
        for(final Bundle bundle : bundles) {
            try {
                final File file = FileLocator.getBundleFile(bundle);
                final String path = file.getAbsolutePath();
                if(path.endsWith(".jar")) {
                    // An installed JAR plugin.
                    classpath.add(path);
                } else {
                    final File targetClasses = Paths.get(path, "target", "classes").toFile();
                    final File bin = Paths.get(path, "bin").toFile();
                    if(targetClasses.exists()) {
                        // A plugin under development with all its classes in the target/classes directory.
                        classpath.add(targetClasses.getAbsolutePath());
                    } else if(bin.exists()) {
                        // A plugin under development with all its classes in the bin directory.
                        classpath.add(bin.getAbsolutePath());
                    } else {
                        // An installed unpacked plugin. Class files are extracted in this directory.
                        classpath.add(path);
                    }

                    // Also include any nested jar files.
                    final File[] jarFiles = file.listFiles(new FilenameFilter() {
                        @Override public boolean accept(File dir, String name) {
                            return name.endsWith(".jar");
                        }
                    });
                    for(File jarFile : jarFiles) {
                        classpath.add(jarFile.getAbsolutePath());
                    }
                }
            } catch(IOException e) {

            }
        }

        return Joiner.on(File.pathSeparator).join(classpath);
    }
}
