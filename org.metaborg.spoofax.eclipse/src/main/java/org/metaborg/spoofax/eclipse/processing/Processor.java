package org.metaborg.spoofax.eclipse.processing;

import java.util.Set;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.language.LanguageAddedJob;
import org.metaborg.spoofax.eclipse.language.LanguageInvalidatedJob;
import org.metaborg.spoofax.eclipse.language.LanguageReloadedActiveJob;
import org.metaborg.spoofax.eclipse.language.LanguageRemovedJob;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;

import rx.functions.Action1;

import com.google.inject.Inject;

public class Processor {
    // private static final Logger logger = LoggerFactory.getLogger(Processor.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final Set<ILanguageCache> languageCaches;

    private final ISpoofaxEditorListener spoofaxEditorListener;
    private final GlobalSchedulingRules globalRules;

    private final IWorkspace workspace;
    private final IEditorRegistry editorRegistry;


    @Inject public Processor(IEclipseResourceService resourceService, ILanguageService languageService,
        ILanguageIdentifierService languageIdentifierService, ILanguageDiscoveryService languageDiscoveryService,
        Set<ILanguageCache> languageCaches, ISpoofaxEditorListener spoofaxEditorListener,
        GlobalSchedulingRules globalRules) {
        this.resourceService = resourceService;
        this.languageService = languageService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.languageCaches = languageCaches;

        this.spoofaxEditorListener = spoofaxEditorListener;
        this.globalRules = globalRules;

        this.workspace = ResourcesPlugin.getWorkspace();
        this.editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();

        this.languageService.changes().subscribe(new Action1<LanguageChange>() {
            @Override public void call(LanguageChange change) {
                languageChange(change);
            }
        });
    }

    public void startup() {
        final Job job = new StartupJob(resourceService, languageDiscoveryService);
        job.setRule(new MultiRule(new ISchedulingRule[] { globalRules.startupWriteLock(),
            globalRules.languageServiceLock() }));
        job.schedule();
    }

    // public void projectOpen(IProject project) {
    // // TODO: Check if there is a language inside this project, if so, load it.
    // }
    //
    // public void projectClose(IProject project) {
    // // TODO: Check if there is a loaded language inside this project, if so, unload it.
    // // TODO: Cancel all build jobs in this project.
    // // TODO: Cancel all update jobs in this project, this may happen automatically because all
    // // editors inside this project will be closed when the project is closed.
    // }


    private void languageChange(LanguageChange change) {
        final Job changeJob;
        switch(change.kind) {
            case ADD_FIRST:
                changeJob = new LanguageAddedJob(spoofaxEditorListener, editorRegistry, change.newLanguage);
                break;
            case REPLACE_ACTIVE:
            case RELOAD_ACTIVE:
                changeJob =
                    new LanguageReloadedActiveJob(languageCaches, spoofaxEditorListener, editorRegistry,
                        change.oldLanguage, change.newLanguage);
                break;
            case RELOAD:
            case REMOVE:
                changeJob = new LanguageInvalidatedJob(languageCaches, change.oldLanguage);
                break;
            case REMOVE_LAST:
                changeJob =
                    new LanguageRemovedJob(resourceService, languageIdentifierService, spoofaxEditorListener,
                        editorRegistry, workspace, change.oldLanguage);
                break;
            default:
                changeJob = null;
                break;
        }

        if(changeJob == null) {
            return;
        }

        changeJob.setRule(workspace.getRoot());
        changeJob.schedule();
    }
}
