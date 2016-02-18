package org.metaborg.spoofax.meta.core;

import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;

/**
 * Language specification build input arguments.
 */
public class SpoofaxLanguageSpecBuildInput {
    public final ISpoofaxLanguageSpec languageSpec;


    public SpoofaxLanguageSpecBuildInput(ISpoofaxLanguageSpec languageSpec) {
        this.languageSpec = languageSpec;
    }
}
