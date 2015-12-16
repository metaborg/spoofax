package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.spoofax.generator.util.MustacheWriter;
import org.metaborg.util.file.FileAccess;

public abstract class NewBaseGenerator {
    protected final MustacheWriter writer;


    public NewBaseGenerator(LanguageSpecGeneratorScope scope, FileAccess access) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass(), access);
    }
    
    public NewBaseGenerator(LanguageSpecGeneratorScope scope) {
        this(scope, null);
    }
}
