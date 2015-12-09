package org.metaborg.spoofax.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPaths;
import org.metaborg.core.project.ILanguageSpecPathsService;
import org.metaborg.core.project.configuration.ConfigurationBasedConfigService;
import org.metaborg.core.project.configuration.IConfigurationBasedConfig;
import org.metaborg.core.project.configuration.ConfigurationReaderWriter;

public class ConfigurationBasedSpoofaxLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig> implements ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {

    private final ILanguageSpecPathsService languageSpecPathsService;

    private final ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder;

    public ConfigurationBasedSpoofaxLanguageSpecConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ILanguageSpecPathsService languageSpecPathsService, final ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter, null);

        this.languageSpecPathsService = languageSpecPathsService;
        this.configBuilder = configBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        return this.languageSpecPathsService.get(languageSpec).configFile();
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
