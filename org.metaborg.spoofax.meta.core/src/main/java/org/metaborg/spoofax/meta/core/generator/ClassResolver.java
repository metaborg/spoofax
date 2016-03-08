package org.metaborg.spoofax.meta.core.generator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.github.mustachejava.MustacheResolver;

public class ClassResolver implements MustacheResolver {
    private final Class<?> clazz;


    public ClassResolver(Class<?> clazz) {
        this.clazz = clazz;
    }


    @Override public Reader getReader(String resourceName) {
        final InputStream stream = clazz.getResourceAsStream(resourceName);
        if(stream == null) {
            return null;
        }
        return new InputStreamReader(stream);
    }
}
