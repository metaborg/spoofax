package org.metaborg.spoofax.meta.core;

import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.project.SpoofaxProjectSettings;

public class MetaBuildInput {
    public final IProject project;
    public final SpoofaxProjectSettings projectSettings;


    public MetaBuildInput(IProject project, SpoofaxProjectSettings projectSettings) {
        this.project = project;
        this.projectSettings = projectSettings;
    }
}
