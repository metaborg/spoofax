package org.metaborg.spoofax.meta.core.build;

import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;

/**
 * Language specification build input arguments.
 */
public class LanguageSpecBuildInput extends ProjectBuildInput {
    private final ISpoofaxLanguageSpec languageSpec;

    public ISpoofaxLanguageSpec languageSpec() {
        return languageSpec;
    }

    public LanguageSpecBuildInput(final ISpoofaxLanguageSpec languageSpec) {
        super(languageSpec);
        this.languageSpec = languageSpec;
    }
}
