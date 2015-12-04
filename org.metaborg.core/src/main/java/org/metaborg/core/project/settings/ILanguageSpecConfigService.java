package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves language specification configurations.
 *
 * @param <T> The type of configuration.
 */
public interface ILanguageSpecConfigService<TConfig extends ILanguageSpecConfig> extends IConfigService<ILanguageSpec, TConfig> {

//    /**
//     * Gets the configuration for the given language specification.
//     *
//     * @param languageSpec The language specification to get the configuration for.
//     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
//     */
//    @Nullable
//    T get(ILanguageSpec languageSpec) throws IOException, ConfigurationException;
//
//    /**
//     * Sets the configuration for the given language specification.
//     *
//     * @param languageSpec The language specification to set the configuration for.
//     * @param config The configuration; or <code>null</code> to remove an existing configuration.
//     */
//    void set(ILanguageSpec languageSpec, @Nullable T config) throws IOException, ConfigurationException;

}
