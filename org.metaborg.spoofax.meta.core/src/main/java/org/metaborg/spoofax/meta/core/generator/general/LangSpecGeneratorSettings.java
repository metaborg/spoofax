package org.metaborg.spoofax.meta.core.generator.general;

import java.util.Collection;

import org.metaborg.spoofax.meta.core.generator.GeneratorSettings;

public class LangSpecGeneratorSettings {
    public final GeneratorSettings generatorSettings;
    public final Collection<String> extensions;
    public final SyntaxType syntaxType;
    public final TransformationType transformationType;
    public final boolean analysisIncremental;
    public final boolean directoryBasedGrouping;


    public LangSpecGeneratorSettings(GeneratorSettings generatorSettings, Collection<String> extensions,
        SyntaxType syntaxType, TransformationType transformationType, boolean analysisIncremental,
        boolean directoryBasedGrouping) {
        this.generatorSettings = generatorSettings;
        this.extensions = extensions;
        this.syntaxType = syntaxType;
        this.transformationType = transformationType;
        this.analysisIncremental = analysisIncremental;
        this.directoryBasedGrouping = directoryBasedGrouping;
    }
}
