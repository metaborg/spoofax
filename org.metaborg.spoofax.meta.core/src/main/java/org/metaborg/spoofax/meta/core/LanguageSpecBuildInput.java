package org.metaborg.spoofax.meta.core;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.settings.ILanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

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
