package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.project.ILanguageSpec;

/**
 * Stores and retrieves language specification configurations.
 */
public interface ILanguageSpecConfigService {

    /**
     * Gets the configuration for the given language specification.
     *
     * @param languageSpec The language specification to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    ILanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException;

}
