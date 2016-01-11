package org.metaborg.core.project.configuration;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public abstract class ConfigurationBasedConfigService<TSubject, TConfig> {

    private final ConfigurationReaderWriter configurationReaderWriter;

    /**
     * Initializes a new instance of the {@link ConfigurationBasedConfigService} class.
     *
     * @param configurationReaderWriter The configuration reader/writer.
     */
    protected ConfigurationBasedConfigService(
            final ConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }

    /**
     * Gets the configuration for the given subject.
     *
     * @param subject The subject to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    public TConfig get(TSubject subject) throws IOException {
        return getFromConfigFile(getConfigFile(subject));
    }

    /**
     * Gets the configuration from the given file.
     *
     * @param configFile The configuration file with the configuration.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable
    public TConfig getFromConfigFile(FileObject configFile) throws IOException {
        HierarchicalConfiguration<ImmutableNode> configuration;
        try {
            configuration = readConfig(configFile);
        } catch (ConfigurationException e) {
            throw new ConfigurationRuntimeException(e);
        }
        if (configuration != null)
            return toConfig(configuration);
        else
            return null;
    }

    /**
     * Writes the configuration for the given subject.
     *
     * @param subject The subject to set the configuration for.
     * @param config The configuration; or <code>null</code> to remove an existing configuration.
     */
    public void write(TSubject subject, TConfig config) throws IOException {
        FileObject configFile = getConfigFile(subject);
        HierarchicalConfiguration<ImmutableNode> configuration = fromConfig(config);
        try {
            writeConfig(configFile, configuration);
        } catch (ConfigurationException e) {
            throw new ConfigurationRuntimeException(e);
        }
    }

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param subject The subject.
     * @return The configuration file.
     * @throws FileSystemException
     */
    public abstract FileObject getConfigFile(TSubject subject) throws FileSystemException;

    /**
     * Creates a new instance of the config type for the specified configuration.
     *
     * @param configuration The configuration.
     */
    protected abstract TConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration);

    /**
     * Creates a new hierarchical configuration for the specified config object.
     *
     * @param config The config object.
     * @return The configuration.
     */
    protected abstract HierarchicalConfiguration<ImmutableNode> fromConfig(TConfig config);

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
