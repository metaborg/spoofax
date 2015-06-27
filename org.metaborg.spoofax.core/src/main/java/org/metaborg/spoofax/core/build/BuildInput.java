package org.metaborg.spoofax.core.build;

import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.resource.IResourceChange;

public class BuildInput {
    /**
     * Project to build.
     */
    public final IProject project;

    /**
     * Resources that have changed.
     */
    public final Iterable<IResourceChange> resourceChanges;

    /**
     * Language build order.
     */
    public final BuildOrder buildOrder;


    public BuildInput(IProject location, Iterable<IResourceChange> resourceChanges, Iterable<ILanguage> languages) {
        this.project = location;
        this.resourceChanges = resourceChanges;
        this.buildOrder = new BuildOrder(languages);
    }
}
