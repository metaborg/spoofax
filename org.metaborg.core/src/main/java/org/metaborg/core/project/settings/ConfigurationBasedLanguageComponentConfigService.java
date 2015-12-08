package org.metaborg.core.project.settings;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.configuration.ConfigurationBasedConfigService;
import org.metaborg.core.project.configuration.ILanguageComponentConfigService;

import java.io.IOException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public final class ConfigurationBasedLanguageXComponentConfigService<TConfig extends ConfigurationBasedLanguageComponentConfig> extends ConfigurationBasedConfigService<ILanguageComponent, TConfig> implements ILanguageComponentConfigService<TConfig> {

    @Inject
    public ConfigurationBasedLanguageComponentConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter,
            final IConfigurationBasedConfigFactory<TConfig> configFactory) {
        super(configurationReaderWriter, configFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TConfig get(final FileObject rootFolder) throws IOException, ConfigurationException {
        return readConfigFile(getConfigFile(rootFolder));
    }

    /**
     * Gets the configuration file for the specified language component.
     *
     * @param languageComponent The language component.
     * @return The configuration file.
     * @throws FileSystemException
     */
    @Override
    protected FileObject getConfigFile(final ILanguageComponent languageComponent) throws FileSystemException {
        return getConfigFile(languageComponent.location());
    }

    /**
     * Gets the configuration file for the specified language root folder.
     *
     * @param rootFolder The language root folder.
     * @return The configuration file.
     * @throws FileSystemException
     */
    protected FileObject getConfigFile(final FileObject rootFolder) throws FileSystemException {
        // TODO: Use ILanguageComponentPathsService to get path to config file.
        return rootFolder.resolveFile("metaborg.yml");
    }
}
