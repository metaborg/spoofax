package org.metaborg.spoofax.nativebundle;

import java.net.URL;
import org.apache.commons.lang3.SystemUtils;

public class NativeBundle {
 
    public static URL getNative() {
        if ( SystemUtils.IS_OS_WINDOWS ) {
            return NativeBundle.class.getResource("native/cygwin");
        } else if ( SystemUtils.IS_OS_MAC_OSX ) {
            return NativeBundle.class.getResource("native/macosx");
        } else if ( SystemUtils.IS_OS_LINUX ) {
            return NativeBundle.class.getResource("native/linux");
        } else {
            throw new UnsupportedOperationException("Unsupported platform "+SystemUtils.OS_NAME);
        }
    }

    public static URL getDist() {
        return NativeBundle.class.getResource("dist");
    }

    private NativeBundle() {
    }

}
