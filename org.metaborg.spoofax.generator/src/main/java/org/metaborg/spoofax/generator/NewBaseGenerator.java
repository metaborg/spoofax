package org.metaborg.spoofax.generator;

import org.metaborg.spoofax.generator.project.LanguageSpecGeneratorScope;
import org.metaborg.spoofax.generator.util.MustacheWriter;

public abstract class NewBaseGenerator {
    protected final MustacheWriter writer;


    public NewBaseGenerator(LanguageSpecGeneratorScope scope) {
        this.writer = new MustacheWriter(scope.location(), new Object[] { this, scope }, getClass());
    }
}
