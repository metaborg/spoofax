package org.metaborg.core.project;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ConfigBasedProjectService implements IProjectService {
    private static final ILogger logger = LoggerUtils.logger(ConfigBasedProjectService.class);

    private final ConcurrentMap<FileName, IProject> projects = Maps.newConcurrentMap();

    private final ISourceTextService sourceTextService;
    private final IProjectConfigService projectConfigService;
    
    @Inject public ConfigBasedProjectService(ISourceTextService sourceTextService,
        IProjectConfigService projectConfigService) {
        this.sourceTextService = sourceTextService;
        this.projectConfigService = projectConfigService;
    }
 
 
    @Override public IProject get(FileObject resource) {
        IProject project = getProject(resource);
        if(project == null) {
            project = findProject(resource);
        }
        return project;
    }

    private IProject getProject(FileObject resource) {
        for(Map.Entry<FileName,IProject> entry : projects.entrySet()) {
            if(resource.getName().isAncestor(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
 
    private IProject findProject(FileObject resource) {
        try {
            FileObject dir = (resource.isFolder() ? resource : resource.getParent());
            while(dir != null) {
                FileName name = dir.getName();
                if(projectConfigService.available(dir)) {
                    final ConfigRequest<? extends IProjectConfig> configRequest = projectConfigService.get(dir);
                    if(!configRequest.valid()) {
                        logger.error("Errors occurred when retrieving project configuration from project directory {}", dir);
                        configRequest.reportErrors(new StreamMessagePrinter(sourceTextService, false, false, logger));
                    }

                    final IProjectConfig config = configRequest.config();
                    if(config == null) {
                        logger.error("Could not retrieve project configuration from project directory {}", dir);
                        return null;
                    }

                    final IProject project = new Project(dir, config);
                    IProject prevProject;
                    if((prevProject = projects.putIfAbsent(name, project)) != null) {
                        logger.warn("Project with location {} already exists", name);
                        return prevProject;
                    }
                    return project;
                }
                dir = dir.getParent();
            }
        } catch(FileSystemException e) {
            logger.error("Error while searching for project configuration.",e);
        }
        logger.warn("No project configuration file was found for {}.",resource);
        return null;
    }
}
