package org.metaborg.meta.core.project;

import javax.annotation.Nullable;

import org.metaborg.core.project.IProject;

/**
 * Service for getting a language specification.
 */
public interface ILanguageSpecService {
    /**
     * Checks if given project is a language specification.
     * 
     * @param project
     *            Project to check.
     * @return True if project is a language specification, false otherwise.
     */
    boolean available(IProject project);

    /**
     * Gets a language specification from the specified project.
     *
     * @param project
     *            The project.
     * @return The language specification, or <code>null</code> when the project is not a language specification.
     */
    @Nullable ILanguageSpec get(IProject project);
}
