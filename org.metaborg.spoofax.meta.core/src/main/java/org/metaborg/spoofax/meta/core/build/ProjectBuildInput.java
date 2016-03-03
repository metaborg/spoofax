package org.metaborg.spoofax.meta.core.build;

import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;

/**
 * Project build input arguments.
 */
public class ProjectBuildInput {
    private final IProject project;

    public IProject project() {
        return this.project;
    }

    public ProjectBuildInput(final IProject project) {
        this.project = project;
    }
}
