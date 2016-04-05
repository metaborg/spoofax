package org.metaborg.spoofax.meta.core.generator.language;

import java.util.Collection;

import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;

public class LanguageSpecGeneratorSettings {
    public final GeneratorSettings generatorSettings;
    public final Collection<String> extensions;
    public final SyntaxType syntaxType;
    public final AnalysisType analysisType;


    public LanguageSpecGeneratorSettings(GeneratorSettings generatorSettings, Collection<String> extensions,
        SyntaxType syntaxType, AnalysisType analysisType) {
        this.generatorSettings = generatorSettings;
        this.extensions = extensions;
        this.syntaxType = syntaxType;
        this.analysisType = analysisType;
    }
}
