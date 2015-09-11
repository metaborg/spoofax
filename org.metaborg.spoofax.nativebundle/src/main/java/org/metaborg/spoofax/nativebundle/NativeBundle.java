package org.metaborg.spoofax.nativebundle;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.SystemUtils;

public class NativeBundle {
    public static URI getNative() {
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

    public static URI getDist() {
        return getResource("dist");
    }

    public static URI getStrategoMix() {
        return getResource("dist/StrategoMix.def");
    }
    
    public static URI getImplodePT() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return getNative().resolve("implodePT.exe");
        } else {
            return getNative().resolve("implodePT");
        }
    }

    public static URI getSdf2Table() {
        if(SystemUtils.IS_OS_WINDOWS) {
            return getNative().resolve("sdf2table.exe");
        } else {
            return getNative().resolve("sdf2table");
        }
    }


    private static URI getResource(String name) {
        final URL url = NativeBundle.class.getResource(name);
        if(url == null) {
            throw new IllegalStateException("Resource " + name + " should be in this package.");
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
