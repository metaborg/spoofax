package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;

import com.google.inject.Inject;

public class ConfigurationBasedLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ILanguageSpecConfig> implements ILanguageSpecConfigService, ILanguageSpecConfigWriter {

    private final ConfigurationBasedLanguageSpecConfigBuilder configBuilder;

    @Inject
    public ConfigurationBasedLanguageSpecConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ConfigurationBasedLanguageSpecConfigBuilder configBuilder) {
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
    public  FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
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
            config = this.configBuilder.build(null);
        }
        return ((IConfigurationBasedConfig)config).getConfiguration();
    }

}
