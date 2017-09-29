package org.metaborg.meta.core.config;

import java.util.Collection;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.config.AConfigService;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.ProjectConfig;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.meta.core.project.ILanguageSpec;

import com.google.inject.Inject;

public class LanguageSpecConfigService extends AConfigService<ILanguageSpec, ILanguageSpecConfig>
    implements ILanguageSpecConfigService, ILanguageSpecConfigWriter {
    private final LanguageSpecConfigBuilder configBuilder;


    @Inject public LanguageSpecConfigService(AConfigurationReaderWriter configReaderWriter,
        LanguageSpecConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootDirectory(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    @Override protected FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ConfigRequest<ILanguageSpecConfig> toConfig(HierarchicalConfiguration<ImmutableNode> config,
        FileObject configFile) {
        final ProjectConfig projectConfig = new ProjectConfig(config);
        final LanguageSpecConfig languageSpecConfig = new LanguageSpecConfig(config, projectConfig);
        final MessageBuilder mb = MessageBuilder.create().asError().asInternal().withSource(configFile);
        final Collection<IMessage> messages = languageSpecConfig.validate(mb);
        return new ConfigRequest<>(languageSpecConfig, messages);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageSpecConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build(null);
        }
        return ((IConfig) config).getConfig();
    }
}
