package org.metaborg.spoofax.meta.core;

import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecPaths;

/**
 * Language specification build input arguments.
 */
public class LanguageSpecBuildInput {

    public final ILanguageSpec languageSpec;
    public final ISpoofaxLanguageSpecConfig config;
    public final ISpoofaxLanguageSpecPaths paths;

    public LanguageSpecBuildInput(final ILanguageSpec languageSpec, final ISpoofaxLanguageSpecConfig config, final ISpoofaxLanguageSpecPaths paths) {
        this.languageSpec = languageSpec;
        this.config = config;
        this.paths = paths;
    }
}
