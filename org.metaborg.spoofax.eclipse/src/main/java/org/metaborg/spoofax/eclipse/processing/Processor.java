package org.metaborg.spoofax.eclipse.processing;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class Processor {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;

    private final GlobalMutexes mutexes;

    private final IJobManager jobManager;


    @Inject public Processor(IEclipseResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguageDiscoveryService languageDiscoveryService,
        ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService,
        IStylerService<IStrategoTerm, IStrategoTerm> stylerService, GlobalMutexes mutexes) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.syntaxService = syntaxService;
        this.analysisService = analysisService;
        this.categorizerService = categorizerService;
        this.stylerService = stylerService;

        this.mutexes = mutexes;

        this.jobManager = Job.getJobManager();
    }


    /**
     * Notifies that the Spoofax plugin has been started. Schedules a job that loads all languages in open projects.
     */
    public void startup() {
        final Job job =
            new StartupJob(resourceService, languageDiscoveryService, jobManager, mutexes.startupMutex,
                mutexes.languageServiceMutex);
        job.schedule();
    }


    /**
     * Notifies that a language has been loaded.
     * 
     * @param language
     *            Language that was loaded.
     */
    public void languageLoaded(ILanguage language) {
        // TODO: Start update jobs for all editors of this language.
    }

    /**
     * Notifies that a language has been unloaded.
     * 
     * @param language
     *            Language that was unloaded.
     */
    public void languageUnloaded(ILanguage language) {
        // TODO: Cancel all build jobs of this language.
        // TODO: Cancel all update jobs of this language.
        // TODO: Color all editors of this language grey, to indicate that the language is unloaded.
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
    public void editorOpen(IEditorInput input, ISourceViewer viewer, String text) {
        processEditor(input, viewer, text);
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
    public void editorChange(IEditorInput input, ISourceViewer viewer, String text) {
        processEditor(input, viewer, text);
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
    public void editorInputChange(IEditorInput oldInput, IEditorInput newInput, ISourceViewer viewer, String text) {
        cancelUpdateJobs(oldInput);
        processEditor(newInput, viewer, text);
    }

    private void processEditor(IEditorInput input, ISourceViewer viewer, String text) {
        cancelUpdateJobs(input);
        final IFileEditorInput fileInput = (IFileEditorInput) input;
        final Job job =
            new EditorUpdateJob(resourceService, languageIdentifierService, syntaxService, analysisService,
                categorizerService, stylerService, fileInput, viewer, text);
        job.setRule(new MultiRule(new ISchedulingRule[] { mutexes.startupMutex, fileInput.getFile() }));
        job.setSystem(true);
        job.schedule();
    }

    private void cancelUpdateJobs(IEditorInput input) {
        final Job[] existingJobs = jobManager.find(input);
        for(Job job : existingJobs) {
            job.cancel();
        }
    }
}
