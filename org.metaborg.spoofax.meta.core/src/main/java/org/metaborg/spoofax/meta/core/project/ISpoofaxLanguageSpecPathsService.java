package org.metaborg.spoofax.meta.core.project;

import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.meta.core.project.ILanguageSpecPathsService;

/**
 * Service for getting information about a language specification's content paths.
 */
public interface ISpoofaxLanguageSpecPathsService extends ILanguageSpecPathsService {
    /**
     * Gets language specification paths for the specified language specification.
     *
     * @param languageSpec
     *            The language specification.
     * @return The language specification paths.
     */
    ISpoofaxLanguageSpecPaths get(ILanguageSpec languageSpec);
}
