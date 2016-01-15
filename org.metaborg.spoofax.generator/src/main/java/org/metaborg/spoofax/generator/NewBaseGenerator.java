package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.spoofax.generator.util.MustacheWriter;
import org.metaborg.util.file.FileAccess;

import javax.annotation.Nullable;

public abstract class NewBaseGenerator {
    protected final MustacheWriter writer;


    public NewBaseGenerator(LanguageSpecGeneratorScope scope, @Nullable FileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }
    
    public NewBaseGenerator(LanguageSpecGeneratorScope scope) {
        this(scope, null);
    }
}
