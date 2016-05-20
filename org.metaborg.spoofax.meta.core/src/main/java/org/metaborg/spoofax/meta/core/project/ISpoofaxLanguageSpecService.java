package org.metaborg.spoofax.meta.core.project;

import javax.annotation.Nullable;

import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.project.ILanguageSpecService;

/**
 * Service for getting a Spoofax language specification.
 */
public interface ISpoofaxLanguageSpecService extends ILanguageSpecService {
    /**
     * Checks if given project is a Spoofax language specification.
     * 
     * @param project
     *            Project to check.
     * @return True if project is a language specification, false otherwise.
     */
    @Override boolean available(IProject project);

    /**
     * Gets a Spoofax language specification from the specified project.
     *
     * @param project
     *            The project.
     * @return The Spoofax language specification, or <code>null</code> when the project is not a Spoofax language
     *         specification.
     * @throws ConfigException
     *             When reading Spoofax language specification configuration fails.
     */
    @Override @Nullable ISpoofaxLanguageSpec get(IProject project) throws ConfigException;
}
