package org.metaborg.spoofax.generator.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.github.mustachejava.MustacheResolver;

public class ClassResolver implements MustacheResolver {
    private final Class<?> cls;

    
    public ClassResolver(Class<?> cls) {
        this.cls = cls;
    }

    
    @Override public Reader getReader(String resourceName) {
        InputStream is = cls.getResourceAsStream(resourceName);
        if(is == null) {
            return null;
        }
        return new InputStreamReader(is);
    }
}
