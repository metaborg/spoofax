package org.metaborg.spoofax.meta.core.build;

import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;

/**
 * Language specification build input arguments.
 */
public class LanguageSpecBuildInput {
    public final ISpoofaxLanguageSpec languageSpec;


    public LanguageSpecBuildInput(ISpoofaxLanguageSpec languageSpec) {
        this.languageSpec = languageSpec;
    }
}
