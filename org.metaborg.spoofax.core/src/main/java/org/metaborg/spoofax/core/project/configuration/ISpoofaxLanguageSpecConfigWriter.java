package org.metaborg.spoofax.core.project.configuration;

import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;

import java.io.IOException;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ISpoofaxLanguageSpecConfigWriter {

    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @param config The configuration to write.
     */
    void write(ILanguageSpec languageSpec, ISpoofaxLanguageSpecConfig config) throws IOException;

}
