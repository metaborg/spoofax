package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves language component configurations.
 *
 * @param <TConfig> The type of configuration.
 */
public interface ILanguageComponentConfigService<TConfig extends ILanguageComponentConfig> extends IConfigService<ILanguageComponent, TConfig> {

    /**
     * Gets the configuration for the language component at the given location.
     *
     * @param location The language component location.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     * @throws ConfigurationException
     */
    TConfig get(FileObject location) throws IOException, ConfigurationException;

//    /**
//     * Gets the configuration for the given language component.
//     *
//     * @param languageComponent The language component to get the configuration for.
//     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
//     */
//    @Nullable
//    T get(ILanguageComponent languageComponent) throws IOException, ConfigurationException;
//
//    /**
//     * Sets the configuration for the given language component.
//     *
//     * @param languageComponent The language component to set the configuration for.
//     * @param config The configuration; or <code>null</code> to remove an existing configuration.
//     */
//    void set(ILanguageComponent languageComponent, @Nullable T config) throws IOException,
//            ConfigurationException;
}
