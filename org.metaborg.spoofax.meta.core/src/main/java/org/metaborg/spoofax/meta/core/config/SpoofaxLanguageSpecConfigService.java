package org.metaborg.spoofax.meta.core.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.project.config.AConfigService;
import org.metaborg.core.project.config.AConfigurationReaderWriter;
import org.metaborg.core.project.config.IConfig;
import org.metaborg.meta.core.project.ILanguageSpec;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecConfigService extends AConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig>
    implements ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {
    private final SpoofaxLanguageSpecConfigBuilder configBuilder;


    @Inject public SpoofaxLanguageSpecConfigService(AConfigurationReaderWriter configReaderWriter,
        SpoofaxLanguageSpecConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootFolder(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    @Override public FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ISpoofaxLanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new SpoofaxLanguageSpecConfig(configuration);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ISpoofaxLanguageSpecConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build(null);
        }
        return ((IConfig) config).getConfig();
    }
}
