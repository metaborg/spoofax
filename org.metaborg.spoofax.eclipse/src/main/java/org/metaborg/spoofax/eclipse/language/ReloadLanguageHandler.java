package org.metaborg.spoofax.eclipse.language;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.processing.GlobalMutexes;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

import com.google.inject.Injector;

public class ReloadLanguageHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final GlobalMutexes mutexes;


    public ReloadLanguageHandler() {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageService = injector.getInstance(ILanguageService.class);
        this.languageDiscoveryService = injector.getInstance(ILanguageDiscoveryService.class);
        this.mutexes = injector.getInstance(GlobalMutexes.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null)
            return null;

        final FileObject location = resourceService.resolve(project);
        final Job job = new ReloadLanguageJob(languageService, languageDiscoveryService, location);
        job.setRule(new MultiRule(new ISchedulingRule[] { mutexes.startupMutex, mutexes.languageServiceMutex }));
        job.schedule();

        return null;
    }
}
