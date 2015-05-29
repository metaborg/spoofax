package org.metaborg.spoofax.eclipse.meta.legacy.build;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.FileLocator;
import org.metaborg.spoofax.eclipse.meta.legacy.SpoofaxMetaLegacyPlugin;
import org.metaborg.spoofax.eclipse.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.strategoxt.imp.generator.sdf2imp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LegacyBuildProperties {
    private static String nativeBundleId = "org.metaborg.spoofax.nativebundle";

    private static final Logger logger = LoggerFactory.getLogger(LegacyBuildProperties.class);


    public static Map<String, String> properties() throws IOException {
        final Map<String, String> properties = Maps.newHashMap();
        properties.put("eclipse.spoofaximp.nativeprefix", nativePrefix());
        properties.put("eclipse.spoofaximp.strategominjar", strategoMinJar());
        properties.put("eclipse.spoofaximp.strategojar", strategoJar());
        properties.put("eclipse.spoofaximp.jars", jars());
        return properties;
    }
    
    public static Collection<URL> classpaths() throws MalformedURLException {
        final Collection<URL> classpaths = Lists.newLinkedList();
        classpaths.add(new File(strategoJar()).toURI().toURL());
        classpaths.add(Paths.get(jars(), "make_permissive.jar").toFile().toURI().toURL());
        
        return classpaths;
    }

    
    private static String nativePrefix() throws IOException {
        final Map<String, Bundle> bundles = BundleUtils.bundlesBySymbolicName(SpoofaxMetaLegacyPlugin.context());
        final Bundle nativeBundle = bundles.get(nativeBundleId);
        if(nativeBundle == null) {
            logger.error("Cannot create native prefix, could not find bundle '{}', language build will probably fail",
                nativeBundleId);
            return "";
        }

        final File file = FileLocator.getBundleFile(nativeBundle);
        final String path = file.getAbsolutePath();
        final String postfix;
        if(SystemUtils.IS_OS_WINDOWS) {
            postfix = "cygwin";
        } else if(SystemUtils.IS_OS_LINUX) {
            postfix = "linux";
        } else if(SystemUtils.IS_OS_MAC) {
            postfix = "macosx";
        } else {
            logger.error(
                "Cannot create native prefix, incompatible operating system '{}', language build will probably fail",
                SystemUtils.OS_NAME);
            return "";
        }

        return Paths.get(path, "native", postfix).toString();
    }

    private static String strategoMinJar() {
        return org.strategoxt.stratego_lib.Main.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    }

    private static String strategoJar() {
        String result = org.strategoxt.lang.Context.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if(SystemUtils.IS_OS_WINDOWS) {
            // Fix path on Windows.
            result = result.substring(1);
        }
        if(!result.endsWith(".jar")) {
            // Ensure correct JAR file at development time.
            String result2 = result + "/../strategoxt.jar";
            if(new File(result2).exists())
                return result2;
            result2 = result + "/java/strategoxt.jar";
            if(new File(result2).exists())
                return result2;
        }
        return result;
    }

    private static String jars() {
        final String generatorPath = sdf2imp.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        return Paths.get(new File(generatorPath).getAbsolutePath(), "dist").toFile().getAbsolutePath();
    }
}
