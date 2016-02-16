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

public class ConfigurationBasedSpoofaxLanguageSpecConfigService extends
    ConfigurationBasedConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig> implements
    ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {
    private final ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder;


    @Inject public ConfigurationBasedSpoofaxLanguageSpecConfigService(
        ConfigurationReaderWriter configurationReaderWriter,
        ConfigurationBasedSpoofaxLanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootFolder(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    @Override public FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ISpoofaxLanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedSpoofaxLanguageSpecConfig(configuration);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ISpoofaxLanguageSpecConfig config) {
        if(!(config instanceof IConfigurationBasedConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build(null);
        }
        return ((IConfigurationBasedConfig) config).getConfiguration();
    }
}
