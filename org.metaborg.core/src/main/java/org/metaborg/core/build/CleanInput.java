package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.project.IProject;

/**
 * Input for a clean build.
 */
public class CleanInput {
    /**
     * Project to clean.
     */
    public final IProject project;

    /**
     * File selector to determine which resources are eligible for cleaning, or null to allow everything to be cleaned.
     */
    public final @Nullable FileSelector selector;


    public CleanInput(IProject project, FileSelector selector) {
        this.project = project;
        this.selector = selector;
    }
}
