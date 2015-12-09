package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves Spoofax language specification configurations.
 */
public interface ISpoofaxLanguageSpecConfigService extends ILanguageSpecConfigService {

    /**
     * Gets the configuration for the given language specification.
     *
     * @param languageSpec The language specification to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    @Override
    ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException;

}
