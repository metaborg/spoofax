package org.metaborg.spoofax.eclipse.meta.language;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.meta.SpoofaxMetaPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.AbstractHandlerUtils;

import com.google.inject.Injector;

public class UnloadLanguageHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final GlobalSchedulingRules mutexes;


    public UnloadLanguageHandler() {
        final Injector injector = SpoofaxMetaPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageService = injector.getInstance(ILanguageService.class);
        this.mutexes = injector.getInstance(GlobalSchedulingRules.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IProject project = AbstractHandlerUtils.getProjectFromSelected(event);
        if(project == null)
            return null;

        final FileObject location = resourceService.resolve(project);
        final Job job = new UnloadLanguageJob(languageService, location);
        job.setRule(new MultiRule(new ISchedulingRule[] { mutexes.startupReadLock(), mutexes.languageServiceLock() }));
        job.schedule();

        return null;
    }
}
