package org.metaborg.spoofax.core.config;

import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.config.AConfigService;
import org.metaborg.core.config.AConfigurationReaderWriter;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.IConfig;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.core.project.IProject;

import com.google.inject.Inject;

public class SpoofaxProjectConfigService extends AConfigService<IProject, ISpoofaxProjectConfig>
        implements ISpoofaxProjectConfigService, ISpoofaxProjectConfigWriter {
    private final SpoofaxProjectConfigBuilder configBuilder;


    @Inject public SpoofaxProjectConfigService(AConfigurationReaderWriter configReaderWriter,
            SpoofaxProjectConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override public IProjectConfig defaultConfig(FileObject rootFolder) {
        configBuilder.reset();
        return configBuilder.build(rootFolder);
    }

    @Override public @Nullable ISpoofaxProjectConfig get(IProject project) {
        if(project.config() instanceof ISpoofaxProjectConfig) {
            return (ISpoofaxProjectConfig) project.config();
        }
        return null;
    }


    @Override protected FileObject getRootDirectory(IProject project) throws FileSystemException {
        return project.location();
    }

    @Override protected FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ConfigRequest<ISpoofaxProjectConfig> toConfig(HierarchicalConfiguration<ImmutableNode> config,
            FileObject configFile) {
        final SpoofaxProjectConfig projectConfig = new SpoofaxProjectConfig(config);
        final MessageBuilder mb = MessageBuilder.create().asError().asInternal().withSource(configFile);
        final Collection<IMessage> messages = projectConfig.validate(mb);
        return new ConfigRequest<>(projectConfig, messages);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(ISpoofaxProjectConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build((FileObject) null);
        }
        return ((IConfig) config).getConfig();
    }

}