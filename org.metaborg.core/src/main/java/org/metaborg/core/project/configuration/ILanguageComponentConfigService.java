package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.IConfigService;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves language component configurations.
 *
 * @param <TConfig> The type of configuration.
 */
public interface ILanguageComponentConfigService<TConfig extends ILanguageComponentConfig> {

    /**
     * Gets the configuration for the given subject.
     *
     * @param subject The subject to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    TConfig get(ILanguageComponent subject) throws IOException, ConfigurationException;

    /**
     * Gets the configuration for the language component at the given location.
     *
     * @param rootFolder The language component root folder.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     * @throws IOException
     * @throws ConfigurationException
     */
    @Nullable
    TConfig get(FileObject rootFolder) throws IOException, ConfigurationException;

    /**
     * Sets the configuration for the given subject.
     *
     * @param subject The subject to set the configuration for.
     * @param config The configuration; or <code>null</code> to remove an existing configuration.
     */
    void set(ILanguageComponent subject, @Nullable TConfig config) throws IOException, ConfigurationException;
}
