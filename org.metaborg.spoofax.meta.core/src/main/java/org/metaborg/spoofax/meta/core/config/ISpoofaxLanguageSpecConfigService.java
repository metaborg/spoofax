package org.metaborg.spoofax.meta.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.meta.core.config.ILanguageSpecConfigService;
import org.metaborg.meta.core.project.ILanguageSpec;

/**
 * Stores and retrieves Spoofax language specification configurations.
 */
public interface ISpoofaxLanguageSpecConfigService extends ILanguageSpecConfigService {
    /**
     * Gets the configuration for the given language specification.
     *
     * @param languageSpec
     *            The language specification to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException;
}
