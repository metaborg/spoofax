package org.metaborg.core.project.configuration;

import com.google.inject.Inject;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPaths;
import org.metaborg.core.project.ILanguageSpecPathsService;

public class ConfigurationBasedLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ILanguageSpecConfig> implements ILanguageSpecConfigService, ILanguageSpecConfigWriter {

    private final ILanguageSpecPathsService languageSpecPathsService;
    private final ConfigurationBasedLanguageSpecConfigBuilder configBuilder;

    @Inject
    public ConfigurationBasedLanguageSpecConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ILanguageSpecPathsService languageSpecPathsService, final ConfigurationBasedLanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter);

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
    protected ILanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedLanguageSpecConfig(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageSpecConfig config) {
        if (!(config instanceof IConfigurationBasedConfig)) {
            this.configBuilder.reset();
            this.configBuilder.copyFrom(config);
            config = this.configBuilder.build();
        }
        return ((IConfigurationBasedConfig)config).getConfiguration();
    }

}
