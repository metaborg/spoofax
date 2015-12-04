package org.metaborg.spoofax.meta.core;

import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

/**
 * Use {@link LanguageSpecBuildInput} instead.
 */
@Deprecated
public class MetaBuildInput {
    public final IProject project;
    public final SpoofaxProjectSettings settings;


    public MetaBuildInput(IProject project, SpoofaxProjectSettings settings) {
        this.project = project;
        this.settings = settings;
    }
}
