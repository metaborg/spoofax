package org.metaborg.spoofax.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;

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
    @Nullable @Override ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException;
}
