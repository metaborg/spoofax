package org.metaborg.core.config;

import java.util.Collection;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.core.project.IProject;

import com.google.inject.Inject;

public class ProjectConfigService extends AConfigService<IProject, IProjectConfig>
        implements IProjectConfigService, IProjectConfigWriter {
    private final ProjectConfigBuilder configBuilder;


    @Inject public ProjectConfigService(AConfigurationReaderWriter configReaderWriter,
            ProjectConfigBuilder configBuilder) {
        super(configReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override public IProjectConfig defaultConfig(FileObject rootFolder) {
        configBuilder.reset();
        return configBuilder.build(rootFolder);
    }


    @Override protected FileObject getRootDirectory(IProject project) throws FileSystemException {
        return project.location();
    }

    @Override protected FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected ConfigRequest<IProjectConfig> toConfig(HierarchicalConfiguration<ImmutableNode> config,
            FileObject configFile) {
        final ProjectConfig projectConfig = new ProjectConfig(config);
        final MessageBuilder mb = MessageBuilder.create().asError().asInternal().withSource(configFile);
        final Collection<IMessage> messages = projectConfig.validate(mb);
        return new ConfigRequest<IProjectConfig>(projectConfig, messages);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(IProjectConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build((FileObject) null);
        }
        return ((IConfig) config).getConfig();
    }
}
