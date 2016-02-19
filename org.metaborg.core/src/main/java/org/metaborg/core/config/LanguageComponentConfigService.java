package org.metaborg.core.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.ILanguageComponent;

import com.google.inject.Inject;

public class LanguageComponentConfigService extends AConfigService<ILanguageComponent, ILanguageComponentConfig>
    implements ILanguageComponentConfigService, ILanguageComponentConfigWriter {
    private final LanguageComponentConfigBuilder configBuilder;


    @Inject public LanguageComponentConfigService(AConfigurationReaderWriter configReaderWriter,
        LanguageComponentConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootDirectory(ILanguageComponent languageComponent) throws FileSystemException {
        return languageComponent.location();
    }

    @Override protected FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_COMPONENT_CONFIG);
    }

    @Override protected ILanguageComponentConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new LanguageComponentConfig(configuration);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageComponentConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build(null);
        }
        return ((IConfig) config).getConfig();
    }
}
