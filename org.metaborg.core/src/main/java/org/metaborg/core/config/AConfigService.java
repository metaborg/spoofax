package org.metaborg.core.config;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.util.file.FileAccess;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

/**
 * Stores and retrieves configurations using the {@link Configuration} class.
 */
public abstract class AConfigService<TSubject, TConfig> {
    private static final ILogger logger = LoggerUtils.logger(AConfigService.class);

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
     * Checks if a configuration exists for given subject.
     *
     * @param subject
     *            The subject to get the configuration for.
     * @return True if a configuration exists, false otherwise.
     */
    public boolean available(TSubject subject) {
        try {
            return available(getRootDirectory(subject));
        } catch(FileSystemException e) {
            logger.debug("Cannot determine if configuration for subject {} is available", e, subject);
            return false;
        }
    }

    /**
     * Checks if a configuration exists for a subject.
     *
     * @param rootDirectory
     *            The root directory of the subject to get the configuration for.
     * @return True if a configuration exists, false otherwise.
     */
    public boolean available(FileObject rootDirectory) {
        try {
            final FileObject configFile = getConfigFile(rootDirectory);
            return configFile.exists();
        } catch(FileSystemException e) {
            logger.debug("Cannot determine if configuration at root directory {} is available", e, rootDirectory);
            return false;
        }
    }

    /**
     * Gets the configuration for the given subject.
     *
     * @param subject
     *            The subject to get the configuration for.
     * @return The configuration, or <code>null</code> when no configuration could be retrieved.
     */
    public @Nullable TConfig get(TSubject subject) throws ConfigException {
        final FileObject rootDirectory;
        try {
            rootDirectory = getRootDirectory(subject);
        } catch(FileSystemException e) {
            throw new ConfigException("Unable to locate configuration for subject " + subject, e);
        }
        return get(rootDirectory);
    }

    /**
     * Gets the configuration for the a subject.
     *
     * @param rootDirectory
     *            The root directory of the subject to get the configuration for.
     * @return The configuration, or <code>null</code> when no configuration could be retrieved.
     */
    public @Nullable TConfig get(FileObject rootDirectory) throws ConfigException {
        final FileObject configFile;
        try {
            configFile = getConfigFile(rootDirectory);
        } catch(FileSystemException e) {
            throw new ConfigException("Unable to locate configuration for root directory " + rootDirectory, e);
        }
        return getFromConfigFile(configFile, rootDirectory);
    }

    /**
     * Gets the configuration from the given file.
     *
     * @param configFile
     *            The configuration file with the configuration.
     * @return The configuration, or <code>null</code> when no configuration could be retrieved.
     */
    public @Nullable TConfig getFromConfigFile(FileObject configFile, @Nullable FileObject rootFolder)
        throws ConfigException {
        final HierarchicalConfiguration<ImmutableNode> configuration = readConfig(configFile, rootFolder);
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
     *            The configuration, or <code>null</code> to remove an existing configuration.
     * @param access
     */
    public void write(TSubject subject, TConfig config, @Nullable FileAccess access) throws ConfigException {
        final FileObject rootDirectory;
        try {
            rootDirectory = getRootDirectory(subject);
        } catch(FileSystemException e) {
            throw new ConfigException("Unable to locate configuration for subject " + subject, e);
        }
        write(rootDirectory, config, access);
    }

    /**
     * Writes the configuration for the given subject.
     *
     * @param rootDirectory
     *            The root directory of the subject to set the configuration for.
     * @param config
     *            The configuration, or <code>null</code> to remove an existing configuration.
     * @param access
     */
    public void write(FileObject rootDirectory, TConfig config, @Nullable FileAccess access) throws ConfigException {
        final FileObject configFile;
        try {
            configFile = getConfigFile(rootDirectory);
        } catch(FileSystemException e) {
            throw new ConfigException("Unable to locate configuration at root directory " + rootDirectory, e);
        }
        if(access != null) {
            access.addWrite(configFile);
        }
        final HierarchicalConfiguration<ImmutableNode> configuration = fromConfig(config);
        writeConfig(configFile, configuration, rootDirectory);
    }


    /**
     * Reads a configuration from a file.
     *
     * @param configFile
     *            The configuration file to read.
     * @return The read configuration, or <code>null</code> when the configuration could not be read.
     * @throws ConfigException
     */
    protected @Nullable HierarchicalConfiguration<ImmutableNode> readConfig(FileObject configFile,
        @Nullable FileObject rootDirectory) throws ConfigException {
        try {
            if(!configFile.exists()) {
                return null;
            }
        } catch(FileSystemException e) {
            throw new ConfigException("Unable to determine if configuration at " + configFile + " exists", e);
        }
        try {
            return configurationReaderWriter.read(configFile, rootDirectory);
        } catch(IOException | ConfigurationException e) {
            throw new ConfigException("Unable to read configuration from " + configFile, e);
        }
    }

    /**
     * Writes a configuration to a file.
     * 
     * @param configFile
     *            The configuration file to write to.
     * @param config
     *            The configuration to write, or <code>null</code> to delete the configuration file.
     * @param rootDirectory
     *            The root folder.
     * @throws IOException
     * @throws ConfigurationException
     */
    protected void writeConfig(FileObject configFile, @Nullable HierarchicalConfiguration<ImmutableNode> config,
        @Nullable FileObject rootDirectory) throws ConfigException {
        if(config != null) {
            try {
                configurationReaderWriter.write(config, configFile, rootDirectory);
            } catch(IOException | ConfigurationException e) {
                throw new ConfigException("Unable to write configuration to " + configFile, e);
            }
        } else {
            try {
                configFile.delete();
            } catch(FileSystemException e) {
                throw new ConfigException("Unable to delete configuration at " + configFile, e);
            }
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
    protected FileObject getConfigFile(TSubject subject) throws FileSystemException {
        return getConfigFile(getRootDirectory(subject));
    }

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param subject
     *            The subject.
     * @return The configuration file.
     * @throws FileSystemException
     */
    protected abstract FileObject getRootDirectory(TSubject subject) throws FileSystemException;

    /**
     * Gets the configuration file for the specified subject.
     *
     * @param rootFolder
     *            The root folder.
     * @return The configuration file.
     * @throws FileSystemException
     */
    protected abstract FileObject getConfigFile(FileObject rootFolder) throws FileSystemException;

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
}
