package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.IProject;

/**
 * Input for a clean build.
 */
public class CleanInput {
    /**
     * Project to clean.
     */
//    public final IProject project;
    public final ILanguageSpec languageSpec;
    
    /**
     * Languages to run clean operations for.
     */
    public final Iterable<ILanguageImpl> languages;

    /**
     * File selector to determine which resources are eligible for cleaning, or null to allow everything to be cleaned.
     */
    public final @Nullable FileSelector selector;


    public CleanInput(ILanguageSpec languageSpec, Iterable<ILanguageImpl> languages, FileSelector selector) {
//    public CleanInput(IProject project, Iterable<ILanguageImpl> languages, FileSelector selector) {
        this.languageSpec = languageSpec;
        this.languages = languages;
        this.selector = selector;
    }
}
