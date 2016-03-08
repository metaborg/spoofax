package org.metaborg.spoofax.meta.core.build;

import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;

/**
 * Language specification build input arguments.
 */
public class LanguageSpecBuildInput extends ProjectBuildInput {

    public ISpoofaxLanguageSpec languageSpec() {
        return (ISpoofaxLanguageSpec)super.project();
    }

    public LanguageSpecBuildInput(final ISpoofaxLanguageSpec languageSpec) {
        super(languageSpec);
    }
}
