package org.metaborg.spoofax.eclipse.processing;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.job.LockRule;
import org.metaborg.spoofax.eclipse.language.LoadLanguageJob;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class StartupJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(StartupJob.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final IJobManager jobManager;
    private final LockRule startupLock;
    private final LockRule languageServiceLock;


    public StartupJob(IEclipseResourceService resourceService, ILanguageDiscoveryService languageDiscoveryService,
        IJobManager jobManager, LockRule startupLock, LockRule languageServiceLock) {
        super("Loading all Spoofax languages in workspace");
        setPriority(Job.LONG);
        
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.jobManager = jobManager;
        this.startupLock = startupLock;
        this.languageServiceLock = languageServiceLock;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running startup job");

        try {
            // Enable startup lock to defer execution of all other jobs, until all languages are loaded.
            jobManager.beginRule(startupLock, monitor);
            final Collection<Job> jobs = Lists.newLinkedList();
            for(final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if(project.isOpen()) {
                    final FileObject location = resourceService.resolve(project);
                    final Job job = new LoadLanguageJob(languageDiscoveryService, location);
                    job.setRule(languageServiceLock);
                    job.schedule();
                    jobs.add(job);
                }
            }
            for(Job job : jobs) {
                try {
                    job.join();
                } catch(InterruptedException e) {
                    // Ignore interruption, just want to wait until all jobs have completed.
                }
            }
        } finally {
            jobManager.endRule(startupLock);
        }
        return StatusUtils.success();
    }
}
