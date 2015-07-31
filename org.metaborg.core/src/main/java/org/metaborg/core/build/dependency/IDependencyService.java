package org.metaborg.core.build.dependency;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

/**
 * Interface for a service that returns compile-time and runtime dependencies for projects.
 */
public interface IDependencyService {
    /**
     * Gets compile-time dependencies for given project.
     * 
     * @param project
     *            Project to get compile-time dependencies for.
     * @return Compile-time language implementation dependencies.
     * @throws MetaborgException
     *             When getting dependencies fails unexpectedly.
     */
    public abstract Iterable<? extends ILanguageImpl> compileDependencies(IProject project) throws MetaborgException;

    /**
     * Gets runtime dependencies for given project.
     * 
     * @param project
     *            Project to get runtime dependencies for.
     * @return Cruntime language implementation dependencies.
     * @throws MetaborgException
     *             When getting dependencies fails unexpectedly.
     */
    public abstract Iterable<? extends ILanguageImpl> runtimeDependencies(IProject project) throws MetaborgException;
}
