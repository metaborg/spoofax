package org.metaborg.spoofax.meta.core.generator;

import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(GeneratorSettings scope, @Nullable FileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }
    
    public BaseGenerator(GeneratorSettings scope) {
        this(scope, null);
    }
}
