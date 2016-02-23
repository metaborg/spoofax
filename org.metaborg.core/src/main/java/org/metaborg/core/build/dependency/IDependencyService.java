package org.metaborg.core.build.dependency;

import java.util.Collection;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.IProject;

/**
 * Returns compile and source dependencies for a project or language component.
 */
public interface IDependencyService {
    /**
     * Gets compile dependencies for the given project.
     * 
     * @param project
     *            Project to get dependencies for.
     * @return Compile dependencies.
     */
    Collection<ILanguageComponent> compileDeps(IProject project) throws MissingDependencyException;

    /**
     * Gets source dependencies for the given project.
     *
     * @param project
     *            Project to get dependencies for.
     * @return Source dependencies.
     */
    Collection<ILanguageComponent> sourceDeps(IProject project) throws MissingDependencyException;

    /**
     * Gets source dependencies for the given language component.
     *
     * @param component
     *            Language component to get dependencies for.
     * @return Source dependencies.
     */
    Collection<ILanguageComponent> sourceDeps(ILanguageComponent component) throws MissingDependencyException;

    /**
     * Checks if compile and source dependencies for the given project are loaded; and returns any missing dependencies.
     *
     * @param project
     *            Project to check the dependencies for.
     * @return Missing dependencies.
     */
    MissingDependencies checkDependencies(IProject project);
}
