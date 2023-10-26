package org.metaborg.core.build;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.language.ILanguageImpl;
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
     * Languages to run clean operations for.
     */
    public final Iterable<ILanguageImpl> languages;

    /**
     * File selector to determine which resources are eligible for cleaning, or null to allow everything to be cleaned.
     */
    public final @Nullable FileSelector selector;


    public CleanInput(IProject project, Iterable<ILanguageImpl> languages, @Nullable FileSelector selector) {
        this.project = project;
        this.languages = languages;
        this.selector = selector;
    }
}
