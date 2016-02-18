package org.metaborg.core.config;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.project.IProject;

import com.google.inject.Inject;

public class ProjectConfigService extends AConfigService<IProject, IProjectConfig>
    implements IProjectConfigService, IProjectConfigWriter {
    private final ProjectConfigBuilder configBuilder;


    @Inject public ProjectConfigService(AConfigurationReaderWriter configurationReaderWriter,
        ProjectConfigBuilder configBuilder) {
        super(configurationReaderWriter);

        this.configBuilder = configBuilder;
    }


    @Override protected FileObject getRootFolder(IProject project) throws FileSystemException {
        return project.location();
    }

    @Override public FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(MetaborgConstants.FILE_CONFIG);
    }

    @Override protected IProjectConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ProjectConfig(configuration);
    }

    @Override protected HierarchicalConfiguration<ImmutableNode> fromConfig(IProjectConfig config) {
        if(!(config instanceof IConfig)) {
            configBuilder.reset();
            configBuilder.copyFrom(config);
            config = configBuilder.build(null);
        }
        return ((IConfig) config).getConfig();
    }
}
