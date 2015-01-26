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
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.SpoofaxStatus;

import com.google.common.collect.Lists;

public class StartupJob extends Job {
    private final IEclipseResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final IJobManager jobManager;
    private final MutexRule startupMutex;
    private final MutexRule languageServiceMutex;


    public StartupJob(IEclipseResourceService resourceService,
        ILanguageDiscoveryService languageDiscoveryService, IJobManager jobManager,
        MutexRule startupMutex, MutexRule languageServiceMutex) {
        super("Loading all Spoofax languages in workspace");
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.jobManager = jobManager;
        this.startupMutex = startupMutex;
        this.languageServiceMutex = languageServiceMutex;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        try {
            final Collection<Job> jobs = Lists.newLinkedList();
            jobManager.beginRule(startupMutex, monitor);
            for(final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if(project.isOpen()) {
                    final FileObject location = resourceService.resolve(project);
                    final Job job = new LanguageLoadJob(languageDiscoveryService, location);
                    job.setRule(languageServiceMutex);
                    jobs.add(job);
                    job.schedule();
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
            jobManager.endRule(startupMutex);
        }
        return SpoofaxStatus.success();
    }
}
