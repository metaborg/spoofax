package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.configuration.ConfigurationBasedConfigService;
import org.metaborg.core.project.configuration.ConfigurationReaderWriter;
import org.metaborg.core.project.configuration.IConfigurationBasedConfig;

import com.google.inject.Inject;

public class ConfigurationBasedSpoofaxLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig> implements ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {

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
    protected FileObject getRootFolder(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
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
            config = this.configBuilder.build(null);
        }
        return ((IConfigurationBasedConfig)config).getConfiguration();
    }

}
