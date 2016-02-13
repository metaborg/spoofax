package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.spoofax.generator.util.MustacheWriter;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;

public abstract class BaseGenerator {
    protected final MustacheWriter writer;


    public BaseGenerator(LanguageSpecGeneratorScope scope, @Nullable FileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }
    
    public BaseGenerator(LanguageSpecGeneratorScope scope) {
        this(scope, null);
    }
}
