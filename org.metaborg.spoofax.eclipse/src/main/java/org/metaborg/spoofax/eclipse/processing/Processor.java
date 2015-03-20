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
    private final ILanguageIdentifierService languageIdentifier;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final Set<ILanguageCache> languageCaches;

    private final ISpoofaxEditorListener editorListener;
    private final GlobalSchedulingRules globalRules;

    private final IWorkspace workspace;
    private final IEditorRegistry editorRegistry;


    @Inject public Processor(IEclipseResourceService resourceService, ILanguageService languageService,
        ILanguageIdentifierService languageIdentifierService, ILanguageDiscoveryService languageDiscoveryService,
        Set<ILanguageCache> languageCaches, ISpoofaxEditorListener spoofaxEditorListener,
        GlobalSchedulingRules globalRules) {
        this.resourceService = resourceService;
        this.languageService = languageService;
        this.languageIdentifier = languageIdentifierService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.languageCaches = languageCaches;

        this.editorListener = spoofaxEditorListener;
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
        job.setRule(new MultiRule(new ISchedulingRule[] { workspace.getRoot(), globalRules.startupWriteLock(),
            globalRules.languageServiceLock() }));
        job.schedule();
    }


    private void languageChange(LanguageChange change) {
        final Job job;
        switch(change.kind) {
            case ADD_FIRST:
                job = new LanguageAddedJob(editorListener, editorRegistry, change.newLanguage);
                break;
            case REPLACE_ACTIVE:
            case RELOAD_ACTIVE:
                job =
                    new LanguageReloadedActiveJob(languageCaches, editorListener, editorRegistry, change.oldLanguage,
                        change.newLanguage);
                break;
            case RELOAD:
            case REMOVE:
                job = new LanguageInvalidatedJob(languageCaches, change.oldLanguage);
                break;
            case REMOVE_LAST:
                job =
                    new LanguageRemovedJob(resourceService, languageIdentifier, editorListener, editorRegistry,
                        workspace, change.oldLanguage);
                break;
            default:
                job = null;
                break;
        }

        if(job == null) {
            return;
        }

        job.setRule(new MultiRule(new ISchedulingRule[] { workspace.getRoot(), globalRules.startupReadLock(),
            globalRules.languageServiceLock() }));
        job.schedule();
    }
}
