package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.metaborg.core.language.ILanguageComponent;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves configurations.
 *
 * @param <TSubject> The type of subject.
 * @param <TConfig> The type of configuration.
 */
public interface IConfigService<TSubject, TConfig> {

    /**
     * Gets the configuration for the given subject.
     *
     * @param subject The subject to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    TConfig get(TSubject subject) throws IOException, ConfigurationException;

    /**
     * Sets the configuration for the given subject.
     *
     * @param subject The subject to set the configuration for.
     * @param config The configuration; or <code>null</code> to remove an existing configuration.
     */
    void set(TSubject subject, @Nullable TConfig config) throws IOException,
            ConfigurationException;
}
