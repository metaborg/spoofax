package org.metaborg.spoofax.meta.core.config;

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
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.config.SpoofaxProjectConfig;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecConfigService extends AConfigService<ILanguageSpec, ISpoofaxLanguageSpecConfig>
    implements ISpoofaxLanguageSpecConfigService, ISpoofaxLanguageSpecConfigWriter {
    private final SpoofaxLanguageSpecConfigBuilder configBuilder;


    @Inject public SpoofaxLanguageSpecConfigService(AConfigurationReaderWriter configReaderWriter,
        SpoofaxLanguageSpecConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override public boolean exists(ILanguageSpec languageSpec) {
        // HACK: expose available to writer interface through this exist method.
        return available(languageSpec.location());
    }

    @Override protected FileObject getRootDirectory(ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location();
    }

    @Override protected FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ConfigRequest<ISpoofaxLanguageSpecConfig>
        toConfig(HierarchicalConfiguration<ImmutableNode> config, FileObject configFile) {
        final SpoofaxProjectConfig projectConfig = new SpoofaxProjectConfig(config);
        final SpoofaxLanguageSpecConfig languageSpecConfig = new SpoofaxLanguageSpecConfig(config, projectConfig);
        final MessageBuilder mb = MessageBuilder.create().asError().asInternal().withSource(configFile);
        final Collection<IMessage> messages = languageSpecConfig.validate(mb);
        return new ConfigRequest<ISpoofaxLanguageSpecConfig>(languageSpecConfig, messages);
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
