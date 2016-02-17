package org.metaborg.core.project.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.file.FileAccess;

/**
 * Stores and retrieves configurations using the {@link Configuration} class.
 */
public abstract class AConfigService<TSubject, TConfig> {
    private final AConfigurationReaderWriter configurationReaderWriter;


    /**
     * Initializes a new instance of the {@link AConfigService} class.
     *
     * @param configurationReaderWriter
     *            The configuration reader/writer.
     */
    protected AConfigService(AConfigurationReaderWriter configurationReaderWriter) {
        this.configurationReaderWriter = configurationReaderWriter;
    }


    /**
     * Gets the configuration for the given subject.
     *
     * @param subject
     *            The subject to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable public TConfig get(TSubject subject) throws IOException {
        return get(getRootFolder(subject));
    }

    /**
     * Gets the configuration for the given subject.
     *
     * @param rootFolder
     *            The root folder of the subject to get the configuration for.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable public TConfig get(FileObject rootFolder) throws IOException {
        return getFromConfigFile(getConfigFile(rootFolder), rootFolder);
    }

    /**
     * Gets the configuration from the given file.
     *
     * @param configFile
     *            The configuration file with the configuration.
     * @return The configuration; or <code>null</code> when no configuration could be retrieved.
     */
    @Nullable public TConfig getFromConfigFile(FileObject configFile, @Nullable FileObject rootFolder)
        throws IOException {
        final HierarchicalConfiguration<ImmutableNode> configuration;
        try {
            configuration = readConfig(configFile, rootFolder);
        } catch(ConfigurationException e) {
            throw new ConfigurationRuntimeException(e);
        }

        if(configuration != null) {
            return toConfig(configuration);
        } else {
            return null;
        }
    }

    /**
     * Writes the configuration for the given subject.
     *
     * @param subject
     *            The subject to set the configuration for.
     * @param config
     *            The configuration; or <code>null</code> to remove an existing configuration.
     * @param access
     */
    public void write(TSubject subject, TConfig config, @Nullable FileAccess access) throws IOException {
        write(getRootFolder(subject), config, access);
    }

    /**
     * Writes the configuration for the given subject.
     *
     * @param rootFolder
     *            The root folder of the subject to set the configuration for.
     * @param config
     *            The configuration; or <code>null</code> to remove an existing configuration.
     * @param access
     */
    public void write(FileObject rootFolder, TConfig config, @Nullable FileAccess access) throws IOException {
        final FileObject configFile = getConfigFile(rootFolder);
        final HierarchicalConfiguration<ImmutableNode> configuration = fromConfig(config);

        try {
            writeConfig(configFile, configuration, rootFolder);
        } catch(ConfigurationException e) {
            throw new ConfigurationRuntimeException(e);
        }

        if(access != null) {
            access.addWrite(configFile);
        }
    }

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param subject
     *            The subject.
     * @return The configuration file.
     * @throws FileSystemException
     */
    public FileObject getConfigFile(TSubject subject) throws FileSystemException {
        return getConfigFile(getRootFolder(subject));
    }

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param subject
     *            The subject.
     * @return The configuration file.
     * @throws FileSystemException
     */
    protected abstract FileObject getRootFolder(TSubject subject) throws FileSystemException;

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param rootFolder
     *            The root folder.
     * @return The configuration file.
     * @throws FileSystemException
     */
    public abstract FileObject getConfigFile(FileObject rootFolder) throws FileSystemException;

    /**
     * Creates a new instance of the config type for the specified configuration.
     *
     * @param configuration
     *            The configuration.
     */
    protected abstract TConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration);

    /**
     * Creates a new hierarchical configuration for the specified config object.
     *
     * @param config
     *            The config object.
     * @return The configuration.
     */
    protected abstract HierarchicalConfiguration<ImmutableNode> fromConfig(TConfig config);

    /**
     * Reads a configuration from a file.
     *
     * @param configFile
     *            The configuration file to read.
     * @return The read configuration; or <code>null</code> when the configuration could not be read.
     * @throws IOException
     * @throws ConfigurationException
     */
    @Nullable protected HierarchicalConfiguration<ImmutableNode> readConfig(FileObject configFile,
        @Nullable FileObject rootFolder) throws IOException, ConfigurationException {
        if(!configFile.exists()) {
            return null;
        }
        return this.configurationReaderWriter.read(configFile, rootFolder);
    }

    /**
     * Writes a configuration to a file.
     * 
     * @param configFile
     *            The configuration file to write to.
     * @param config
     *            The configuration to write; or <code>null</code> to delete the configuration file.
     * @param rootFolder
     *            The root folder.
     * @throws IOException
     * @throws ConfigurationException
     */
    protected void writeConfig(FileObject configFile, @Nullable HierarchicalConfiguration<ImmutableNode> config,
        @Nullable FileObject rootFolder) throws IOException, ConfigurationException {
        if(config != null) {
            this.configurationReaderWriter.write(config, configFile, rootFolder);
        } else {
            configFile.delete();
        }
    }
}
