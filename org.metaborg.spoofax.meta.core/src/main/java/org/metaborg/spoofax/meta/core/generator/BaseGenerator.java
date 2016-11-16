package org.metaborg.spoofax.meta.core.generator;

import javax.annotation.Nullable;

import org.metaborg.meta.core.mustache.MustacheWriter;
import org.metaborg.util.file.IFileAccess;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(GeneratorSettings scope, @Nullable IFileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }

    public BaseGenerator(GeneratorSettings scope) {
        this(scope, null);
    }
}
