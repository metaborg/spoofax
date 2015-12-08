package org.metaborg.core.project.configuration;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.settings.ConfigurationBasedLanguageComponentConfig;
import org.metaborg.core.project.settings.IConfigurationBasedConfigFactory;
import org.metaborg.core.project.settings.YamlConfigurationReaderWriter;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public final class ConfigurationBasedLanguageComponentConfigService
        extends ConfigurationBasedConfigService
        implements ILanguageComponentConfigService<ILanguageComponentConfig> {

    @Inject
    public ConfigurationBasedLanguageComponentConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter) {
//            final IConfigurationBasedConfigFactory<TConfig> configFactory) {
        super(configurationReaderWriter);//, configFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ILanguageComponentConfig get(ILanguageComponent subject) throws IOException, ConfigurationException {
        FileObject configFile = getConfigFile(subject);
        HierarchicalConfiguration<ImmutableNode> configuration = readConfig(configFile);
        return new ConfigurationBasedLanguageComponentConfig(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ILanguageComponentConfig get(final FileObject rootFolder) throws IOException, ConfigurationException {
        FileObject configFile = getConfigFile(rootFolder);
        HierarchicalConfiguration<ImmutableNode> configuration = readConfig(configFile);
        return new ConfigurationBasedLanguageComponentConfig(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(ILanguageComponent subject, @Nullable ILanguageComponentConfig config) throws IOException, ConfigurationException {
        FileObject configFile = getConfigFile(subject);
        HierarchicalConfiguration<ImmutableNode> configuration = config != null ? ((ConfigurationBasedLanguageComponentConfig)config).getConfiguration() : null;
        writeConfig(configFile, configuration);
    }

    /**
     * Gets the configuration file for the specified language component.
     *
     * @param subject The subject.
     * @return The configuration file.
     * @throws FileSystemException
     */
    protected FileObject getConfigFile(final ILanguageComponent subject) throws FileSystemException {
        return getConfigFile(subject.location());
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
