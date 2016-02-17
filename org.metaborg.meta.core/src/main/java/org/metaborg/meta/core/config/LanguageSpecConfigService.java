package org.metaborg.meta.core.config;

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

public class LanguageSpecConfigService extends AConfigService<ILanguageSpec, ILanguageSpecConfig>
    implements ILanguageSpecConfigService, ILanguageSpecConfigWriter {
    private final LanguageSpecConfigBuilder configBuilder;


    @Inject public LanguageSpecConfigService(final AConfigurationReaderWriter configurationReaderWriter,
        final LanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootFolder(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    @Override public FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ILanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new LanguageSpecConfig(configuration);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageSpecConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = this.configBuilder.build(null);
        }
        return ((IConfig) config).getConfig();
    }
}
