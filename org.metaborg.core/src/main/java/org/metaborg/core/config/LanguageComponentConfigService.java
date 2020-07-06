package org.metaborg.core.config;

import java.util.Collection;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;

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
        return rootFolder.resolveFile(MetaborgConstants.LOC_COMPONENT_CONFIG);
    }

    @Override protected ConfigRequest<ILanguageComponentConfig>
        toConfig(HierarchicalConfiguration<ImmutableNode> config, FileObject configFile) {
        final ProjectConfig projectConfig = new ProjectConfig(config);
        final LanguageComponentConfig languageComponentConfig = new LanguageComponentConfig(config, projectConfig);
        final MessageBuilder mb = MessageBuilder.create().asError().asInternal().withSource(configFile);
        final Collection<IMessage> messages = languageComponentConfig.validate(mb);
        return new ConfigRequest<ILanguageComponentConfig>(languageComponentConfig, messages);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageComponentConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build((FileObject) null);
        }
        return ((IConfig) config).getConfig();
    }
}
