package org.metaborg.core.project.configuration;

import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;

import java.io.IOException;

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
