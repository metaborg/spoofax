package org.metaborg.spoofax.nativebundle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.SystemUtils;

/**
 * Utilities for getting the sdf2table and implodePT executables, and the Stratego mix definition.
 */
public class NativeBundle {
    /**
     * @return URI to the native directory. Can point to a directory on the local file system, or to a directory in a
     *         JAR file.
     */
    public static URI getNativeDirectory() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return getResource("native/cygwin/");
        } else if(SystemUtils.IS_OS_MAC_OSX) {
            return getResource("native/macosx/");
        } else if(SystemUtils.IS_OS_LINUX) {
            return getResource("native/linux/");
        } else {
            throw new UnsupportedOperationException("Unsupported platform " + SystemUtils.OS_NAME);
        }
    }

    /**
     * @return Name of the sdf2table executable, relative to the directory returned from {@link #getNativeDirectory()}.
     */
    public static String getSdf2TableName() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return "sdf2table.exe";
        } else {
            return "sdf2table";
        }
    }

    /**
     * @return Name of the implodePT executable, relative to the directory returned from {@link #getNativeDirectory()}.
     */
    public static String getImplodePTName() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return "implodePT.exe";
        } else {
            return "implodePT";
        }
    }


    /**
     * @return URI to the Stratego mix definition. Can point to a file on the local file system, or to a file in a JAR
     *         file.
     */
    public static URI getStrategoMix() {
        return getResource("dist/StrategoMix.def");
    }


    private static URI getResource(String name) {
        final URL url = NativeBundle.class.getResource(name);
        if(url == null) {
            throw new IllegalStateException("Resource " + name + " cannot be found in the native bundle");
        }
        try {
            return url.toURI();
        } catch(URISyntaxException e) {
            throw new RuntimeException("Cannot get native bundle resource", e);
        }
    }

    private NativeBundle() {
    }
}
