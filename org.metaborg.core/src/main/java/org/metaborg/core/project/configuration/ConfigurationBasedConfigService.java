package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.settings.YamlConfigurationReaderWriter;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public abstract class ConfigurationBasedConfigService {

    private final YamlConfigurationReaderWriter configurationReaderWriter;
//    private final IConfigurationBasedConfigFactory<TConfig> configFactory;

    /**
     * Initializes a new instance of the {@link ConfigurationBasedConfigService} class.
     *
     * @param configurationReaderWriter The configuration reader/writer.
//     * @param configFactory The configuration factory.
     */
    protected ConfigurationBasedConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter) {
//            final IConfigurationBasedConfigFactory<TConfig> configFactory) {
        this.configurationReaderWriter = configurationReaderWriter;
//        this.configFactory = configFactory;
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Nullable
//    @Override
//    public TConfig get(final TSubject subject) throws IOException, ConfigurationException {
//        return readConfigFile(getConfigFile(subject));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void set(final TSubject subject, @Nullable final TConfig configuration) throws IOException,
//            ConfigurationException {
//        writeConfigFile(getConfigFile(subject), configuration);
//    }
//
//    /**
//     * Gets the configuration file for the specified subject.
//     *
//     * @param subject The subject.
//     * @return The configuration file.
//     * @throws FileSystemException
//     */
//    protected abstract FileObject getConfigFile(TSubject subject) throws FileSystemException;
//
//    /**
//     * Creates a new configuration with the specified properties.
//     *
//     * @param configuration The configuration that provides the properties; or <code>null</code>.
//     * @return The created configuration.
//     */
//    protected TConfig createConfiguration(@Nullable HierarchicalConfiguration<ImmutableNode> configuration) {
//        return this.configFactory.create(configuration);
//    }
//
//    /**
//     * Reads a configuration from a file.
//     *
//     * @param configFile The configuration file to read.
//     * @return The read configuration; or <code>null</code> when the configuration could not be read.
//     * @throws IOException
//     * @throws ConfigurationException
//     */
//    @Nullable
//    protected TConfig readConfigFile(final FileObject configFile) throws IOException,
//            ConfigurationException {
//        HierarchicalConfiguration<ImmutableNode> configuration = this.configurationReaderWriter.read(configFile);
//        return createConfiguration(configuration);
//    }
//
//    /**
//     * Writes a configuration to a file.
//     * @param configFile The configuration file to write to.
//     * @param config The configuration to write; or <code>null</code> to delete the configuration file.
//     * @throws IOException
//     * @throws ConfigurationException
//     */
//    protected void writeConfigFile(final FileObject configFile, @Nullable final TConfig config) throws
//            IOException, ConfigurationException {
//
//        if (config != null) {
//            HierarchicalConfiguration<ImmutableNode> configuration = config.getConfiguration();
//            this.configurationReaderWriter.write(configuration, configFile);
//        } else {
//            configFile.delete();
//        }
//    }

    /**
     * Reads a configuration from a file.
     *
     * @param configFile The configuration file to read.
     * @return The read configuration; or <code>null</code> when the configuration could not be read.
     * @throws IOException
     * @throws ConfigurationException
     */
    @Nullable
    protected HierarchicalConfiguration<ImmutableNode> readConfig(final FileObject configFile) throws IOException,
            ConfigurationException {
        if (!configFile.exists())
            return null;
        return this.configurationReaderWriter.read(configFile);
    }

    /**
     * Writes a configuration to a file.
     * @param configFile The configuration file to write to.
     * @param config The configuration to write; or <code>null</code> to delete the configuration file.
     * @throws IOException
     * @throws ConfigurationException
     */
    protected void writeConfig(final FileObject configFile, @Nullable final HierarchicalConfiguration<ImmutableNode> config) throws
            IOException, ConfigurationException {

        if (config != null) {
            this.configurationReaderWriter.write(config, configFile);
        } else {
            configFile.delete();
        }
    }
}
