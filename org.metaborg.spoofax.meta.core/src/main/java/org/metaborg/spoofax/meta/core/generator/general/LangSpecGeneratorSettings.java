package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Collection;

import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;

public class LangSpecGeneratorSettings {
    public final GeneratorSettings generatorSettings;
    public final Collection<String> extensions;
    public final SyntaxType syntaxType;


    public LangSpecGeneratorSettings(GeneratorSettings generatorSettings, Collection<String> extensions,
        SyntaxType syntaxType) {
        this.generatorSettings = generatorSettings;
        this.extensions = extensions;
        this.syntaxType = syntaxType;
    }
}
