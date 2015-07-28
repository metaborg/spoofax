package org.metaborg.spoofax.nativebundle;

import java.net.URL;
import org.apache.commons.lang3.SystemUtils;

public class NativeBundle {
 
    public static URL getNative() {
        if ( SystemUtils.IS_OS_WINDOWS ) {
            return getResource("native/cygwin");
        } else if ( SystemUtils.IS_OS_MAC_OSX ) {
            return getResource("native/macosx");
        } else if ( SystemUtils.IS_OS_LINUX ) {
            return getResource("native/linux");
        } else {
            throw new UnsupportedOperationException("Unsupported platform "+SystemUtils.OS_NAME);
        }
    }

    public static URL getDist() {
        return getResource("dist");
    }

    private static URL getResource(String name) {
        URL url = NativeBundle.class.getResource(name);
        if ( url == null ) {
            throw new IllegalStateException("Resource "+name+" should be in this package.");
        }
        return url;
    }

    private NativeBundle() {
    }

}
