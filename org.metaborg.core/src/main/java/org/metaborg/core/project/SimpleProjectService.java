package org.metaborg.core.project;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class SimpleProjectService implements ISimpleProjectService {
    private static final ILogger logger = LoggerUtils.logger(SimpleProjectService.class);

    private final ISourceTextService sourceTextService;
    private final IProjectConfigService projectConfigService;

    private final ConcurrentMap<FileName, IProject> projects = Maps.newConcurrentMap();


    @Inject public SimpleProjectService(ISourceTextService sourceTextService,
        IProjectConfigService projectConfigService) {
        this.sourceTextService = sourceTextService;
        this.projectConfigService = projectConfigService;
    }


    @Override public @Nullable IProject get(FileObject resource) {
        final FileName name = resource.getName();
        for(Entry<FileName, IProject> entry : projects.entrySet()) {
            final FileName projectName = entry.getKey();
            if(name.equals(projectName) || name.isAncestor(projectName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override public IProject create(FileObject location) throws MetaborgException {
        final FileName name = location.getName();
        for(FileName projectName : projects.keySet()) {
            if(name.equals(projectName) || name.isAncestor(projectName)) {
                final String message =
                    String.format("Location %s is equal to or nested in project %s", name, projectName);
                throw new MetaborgException(message);
            }
        }

        final ConfigRequest<? extends IProjectConfig> configRequest = projectConfigService.get(location);
        if(!configRequest.valid()) {
            logger.error("Errors occurred when retrieving project configuration from project directory {}", location);
            configRequest.reportErrors(new StreamMessagePrinter(sourceTextService, false, false, logger));
        }

        final IProjectConfig config;
        if(configRequest.config() != null) {
            config = configRequest.config();
        } else {
            logger.info("Using default configuration for project at {}", location);
            config = projectConfigService.defaultConfig(location);
        }

        final IProject project = new Project(location, config);
        if(projects.putIfAbsent(name, project) != null) {
            final String message = String.format("Project with location %s already exists", name);
            throw new MetaborgException(message);
        }
        return project;
    }

    @Override public void remove(IProject project) throws MetaborgException {
        final FileName name = project.location().getName();
        if(projects.remove(name) == null) {
            final String message = String.format("Project with location %s does not exists", name);
            throw new MetaborgException(message);
        }
    }
}
