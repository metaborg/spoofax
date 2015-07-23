package org.metaborg.core.build.dependency;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

/**
 * Interface for a service that returns compile-time and runtime dependencies for projects.
 */
public interface IDependencyService {
    public abstract Iterable<? extends ILanguageImpl> compileDependencies(IProject project);

    public abstract Iterable<? extends ILanguageImpl> runtimeDependencies(IProject project);
}
