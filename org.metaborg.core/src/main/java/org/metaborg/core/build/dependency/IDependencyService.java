package org.metaborg.core.build.dependency;

import java.util.Collection;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;

/**
 * Interface for a service that returns compile-time and runtime dependencies
 * for a language component or specification.
 */
public interface IDependencyService {

    /**
     * Gets compile-time language component dependencies for the given language specification.
     * 
     * @param languageSpec The language specification to get dependencies for.
     * @return Compile-time language component dependencies.
     */
    Collection<ILanguageComponent> compileDependencies(ILanguageSpec languageSpec) throws MissingDependencyException;

    /**
     * Gets runtime language component dependencies for the given language specification.
     *
     * @param languageSpec The language specification to get dependencies for.
     * @return Runtime language component dependencies.
     */
    Collection<ILanguageComponent> runtimeDependencies(ILanguageSpec languageSpec) throws MissingDependencyException;

    /**
     * Gets runtime language component dependencies for the given language component.
     *
     * @param component The language component to get dependencies for.
     * @return Runtime language component dependencies.
     */
    Collection<ILanguageComponent> runtimeDependencies(ILanguageComponent component) throws MissingDependencyException;

    /**
     * Checks if compile-time and runtime dependencies for the given language specification
     * are loaded; and returns any missing dependencies.
     *
     * @param languageSpec The language specification to check the dependencies for.
     * @return The missing compile-time and runtime dependencies.
     */
    MissingDependencies checkDependencies(ILanguageSpec languageSpec);

}
