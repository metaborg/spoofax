package org.metaborg.spoofax.eclipse.processing;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageChange;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.eclipse.editor.EditorUpdateJob;
import org.metaborg.spoofax.eclipse.editor.PresentationMerger;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.language.LanguageAddedJob;
import org.metaborg.spoofax.eclipse.language.LanguageInvalidatedJob;
import org.metaborg.spoofax.eclipse.language.LanguageReloadedActiveJob;
import org.metaborg.spoofax.eclipse.language.LanguageRemovedJob;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import rx.functions.Action1;

import com.google.inject.Inject;

public class Processor {
    private static final Logger logger = LoggerFactory.getLogger(Processor.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final IContextService contextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;
    private final Set<ILanguageCache> languageCaches;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;
    private final GlobalSchedulingRules globalRules;

    private final IWorkspace workspace;
    private final IJobManager jobManager;
    private final IEditorRegistry editorRegistry;


    @Inject public Processor(IEclipseResourceService resourceService, ILanguageService languageService,
        ILanguageIdentifierService languageIdentifierService, ILanguageDiscoveryService languageDiscoveryService,
        IContextService contextService, ISyntaxService<IStrategoTerm> syntaxService,
        IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService,
        IStylerService<IStrategoTerm, IStrategoTerm> stylerService, Set<ILanguageCache> languageCaches,
        ParseResultProcessor parseResultProcessor, AnalysisResultProcessor analysisResultProcessor,
        GlobalSchedulingRules globalRules) {
        this.resourceService = resourceService;
        this.languageService = languageService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.contextService = contextService;
        this.syntaxService = syntaxService;
        this.analysisService = analysisService;
        this.categorizerService = categorizerService;
        this.stylerService = stylerService;
        this.languageCaches = languageCaches;

        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;
        this.globalRules = globalRules;

        this.workspace = ResourcesPlugin.getWorkspace();
        this.jobManager = Job.getJobManager();
        this.editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();

        this.languageService.changes().subscribe(new Action1<LanguageChange>() {
            @Override public void call(LanguageChange change) {
                languageChange(change);
            }
        });
    }

    /**
     * Notifies that the Spoofax plugin has been started. Schedules a job that loads all languages in open projects.
     */
    public void startup() {
        final Job job = new StartupJob(resourceService, languageDiscoveryService);
        job.setRule(new MultiRule(new ISchedulingRule[] { globalRules.startupWriteLock(),
            globalRules.languageServiceLock() }));
        job.schedule();
    }


    private void languageChange(LanguageChange change) {
        final Job changeJob;
        switch(change.kind) {
            case ADD_FIRST:
                changeJob =
                    new LanguageAddedJob(resourceService, languageIdentifierService, editorRegistry, workspace,
                        change.newLanguage);
                break;
            case REPLACE_ACTIVE:
            case RELOAD_ACTIVE:
                changeJob =
                    new LanguageReloadedActiveJob(languageCaches, editorRegistry, change.oldLanguage,
                        change.newLanguage);
                break;
            case RELOAD:
            case REMOVE:
                changeJob = new LanguageInvalidatedJob(languageCaches, change.oldLanguage);
                break;
            case REMOVE_LAST:
                changeJob =
                    new LanguageRemovedJob(resourceService, languageIdentifierService, editorRegistry, workspace,
                        change.oldLanguage);
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


    /**
     * Notifies that a project has been opened.
     * 
     * @param project
     *            Project that was opened.
     */
    public void projectOpen(IProject project) {
        // TODO: Check if there is a language inside this project, if so, load it.
    }

    /**
     * Notifies that a project has been closed.
     * 
     * @param project
     *            Project that was closed.
     */
    public void projectClose(IProject project) {
        // TODO: Check if there is a loaded language inside this project, if so, unload it.
        // TODO: Cancel all build jobs in this project.
        // TODO: Cancel all update jobs in this project, this may happen automatically because all
        // editors inside this project will be closed when the project is closed.
    }


    /**
     * Notifies that a new Spoofax editor has been opened. Schedules an update job for that editor.
     * 
     * @param input
     *            Input object of the editor.
     * @param viewer
     *            Source viewer of the editor.
     * @param text
     *            Initial input text of the editor.
     */
    public void
        editorOpen(IEditorInput input, ISourceViewer viewer, String text, PresentationMerger presentationMerger) {
        processEditor(input, viewer, text, presentationMerger, true);
    }

    /**
     * Notifies that the text in a Spoofax editor has been changed. Cancels existing update jobs for that editor, and
     * schedules a new update job.
     * 
     * @param input
     *            Input object of the editor.
     * @param viewer
     *            Source viewer of the editor.
     * @param text
     *            New input text of the editor.
     */
    public void editorChange(IEditorInput input, ISourceViewer viewer, String text,
        PresentationMerger presentationMerger) {
        processEditor(input, viewer, text, presentationMerger, false);
    }

    /**
     * Notifies that a Spoofax editor has been closed. Cancels existing update job for that editor.
     * 
     * @param input
     *            Input object of the editor.
     */
    public void editorClose(IEditorInput input) {
        cancelUpdateJobs(input);
    }

    /**
     * Notifies that the input object of a Spoofax editor has been changed. Cancels existing update jobs for the old
     * input object, and schedules an update job for the new input object.
     * 
     * @param oldInput
     *            Old input object of the editor.
     * @param newInput
     *            New input object of the editor.
     * @param viewer
     *            Source viewer of the editor.
     * @param text
     *            Input text of the editor.
     */
    public void editorInputChange(IEditorInput oldInput, IEditorInput newInput, ISourceViewer viewer, String text,
        PresentationMerger presentationMerger) {
        logger.debug("Editor input changed from {} to {}", oldInput, newInput);
        cancelUpdateJobs(oldInput);
        processEditor(newInput, viewer, text, presentationMerger, true);
    }

    private void processEditor(IEditorInput input, ISourceViewer viewer, String text,
        PresentationMerger presentationMerger, boolean isNewEditor) {
        cancelUpdateJobs(input);
        // THREADING: invalidate text styling here on the main thread (instead of in the editor update job), to prevent
        // race conditions.
        presentationMerger.invalidate();
        final IFileEditorInput fileInput = (IFileEditorInput) input;
        final IFile file = fileInput.getFile();
        final FileObject resource = resourceService.resolve(file);
        final Job job =
            new EditorUpdateJob(languageIdentifierService, contextService, syntaxService, analysisService,
                categorizerService, stylerService, parseResultProcessor, analysisResultProcessor, fileInput, file,
                resource, viewer, text, presentationMerger, isNewEditor);
        job.setRule(new MultiRule(new ISchedulingRule[] { globalRules.startupReadLock(), file }));
        job.schedule(isNewEditor ? 0 : 100);
    }

    private void cancelUpdateJobs(IEditorInput input) {
        logger.trace("Cancelling editor update jobs for {}", input);
        final Job[] existingJobs = jobManager.find(input);
        for(Job job : existingJobs) {
            job.cancel();
        }
    }
}
