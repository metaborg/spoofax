package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ConfigurationBasedConfigService;
import org.metaborg.core.project.configuration.ConfigurationReaderWriter;
import org.metaborg.core.project.configuration.IConfigurationBasedConfig;

import com.google.inject.Inject;

public class ConfigurationBasedSpoofaxLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig> implements ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {

    public static final String CONFIG_FILE = "metaborg.yml";
    private final ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder;

    @Inject
    public ConfigurationBasedSpoofaxLanguageSpecConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter);

        this.configBuilder = configBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        return getConfigFile(languageSpec.location());
    }

    /**
     * Gets the configuration file for the specified root folder.
     *
     * @param rootFolder The root folder.
     * @return The configuration file.
     * @throws FileSystemException
     */
    private FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(CONFIG_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ISpoofaxLanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedSpoofaxLanguageSpecConfig(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HierarchicalConfiguration<ImmutableNode> fromConfig(ISpoofaxLanguageSpecConfig config) {
        if (!(config instanceof IConfigurationBasedConfig)) {
            this.configBuilder.reset();
            this.configBuilder.copyFrom(config);
            config = this.configBuilder.build();
        }
        return ((IConfigurationBasedConfig)config).getConfiguration();
    }

}
