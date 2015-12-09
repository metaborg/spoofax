package org.metaborg.core.project.configuration;

import org.metaborg.core.project.ILanguageSpec;

import javax.annotation.Nullable;
import java.io.IOException;

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
