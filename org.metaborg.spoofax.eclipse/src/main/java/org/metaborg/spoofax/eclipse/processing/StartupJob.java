package org.metaborg.spoofax.eclipse.processing;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(StartupJob.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;


    public StartupJob(IEclipseResourceService resourceService, ILanguageDiscoveryService languageDiscoveryService) {
        super("Loading all Spoofax languages in workspace");
        setPriority(Job.LONG);

        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running startup job");
        for(final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if(project.isOpen()) {
                final FileObject location = resourceService.resolve(project);
                try {
                    languageDiscoveryService.discover(location);
                } catch(Exception e) {
                    final String message = String.format("Could not load language at location %", location);
                    logger.error(message, e);
                }
            }
        }
        return StatusUtils.success();
    }
}
