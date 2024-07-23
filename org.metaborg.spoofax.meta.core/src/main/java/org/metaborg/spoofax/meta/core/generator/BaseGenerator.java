package org.metaborg.spoofax.meta.core.generator;

import jakarta.annotation.Nullable;

import mb.util.vfs2.file.IFileAccess;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(GeneratorSettings scope, @Nullable IFileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }
    
    public BaseGenerator(GeneratorSettings scope) {
        this(scope, null);
    }
}
