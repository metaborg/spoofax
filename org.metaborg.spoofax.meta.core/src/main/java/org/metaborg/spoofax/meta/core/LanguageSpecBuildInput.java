package org.metaborg.spoofax.meta.core;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

/**
 * Language specification build input arguments.
 */
public class LanguageSpecBuildInput {

    public final ILanguageSpec languageSpec;
    public final ISpoofaxLanguageSpecConfig config;

    public LanguageSpecBuildInput(final ILanguageSpec languageSpec, final ISpoofaxLanguageSpecConfig config) {
        this.languageSpec = languageSpec;
        this.config = config;
    }
}
