package org.metaborg.core.project;

/**
 * Service for getting information about a language specification's content paths.
 */
public interface ILanguageSpecPathsService<T extends ILanguageSpecPaths> {

    /**
     * Gets language specification paths for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @return The language specification paths.
     */
    T get(ILanguageSpec languageSpec);

}
