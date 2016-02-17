package org.metaborg.meta.core.project;

/**
 * Service for getting information about a language specification's content paths.
 */
public interface ILanguageSpecPathsService {
    /**
     * Gets language specification paths for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @return The language specification paths.
     */
    ILanguageSpecPaths get(ILanguageSpec languageSpec);
}
