package org.metaborg.core.project.configuration;

import java.io.IOException;

import org.metaborg.core.project.ILanguageSpec;

/**
 * Writes a configuration for the specified {@link ILanguageSpec}.
 */
public interface ILanguageSpecConfigWriter {

    /**
     * Writes the specified configuration for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @param config The configuration to write.
     */
    void write(ILanguageSpec languageSpec, ILanguageSpecConfig config) throws IOException;

}
