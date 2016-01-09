package org.metaborg.core.project.configuration;

import java.io.IOException;

import org.metaborg.core.language.ILanguageComponent;

/**
 * Writes a configuration for the specified {@link ILanguageComponent}.
 */
public interface ILanguageComponentConfigWriter {

    /**
     * Writes the specified configuration for the specified language component.
     *
     * @param languageComponent The language component.
     * @param config The configuration to write.
     */
    void write(ILanguageComponent languageComponent, ILanguageComponentConfig config) throws IOException;

}
