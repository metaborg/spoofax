package org.metaborg.spoofax.eclipse.processing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class Processor {
    private static final Logger logger = LogManager.getLogger(Processor.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;

    private final IJobManager jobManager;

    private final MutexRule startupMutex = new MutexRule();
    private final MutexRule languageServiceMutex = new MutexRule();


    @Inject public Processor(IEclipseResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService,
        ILanguageDiscoveryService languageDiscoveryService,
        ISyntaxService<IStrategoTerm> syntaxService,
        IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService,
        IStylerService<IStrategoTerm, IStrategoTerm> stylerService) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.syntaxService = syntaxService;
        this.analysisService = analysisService;
        this.categorizerService = categorizerService;
        this.stylerService = stylerService;

        this.jobManager = Job.getJobManager();
    }


    public void startup() {
        final Job job =
            new StartupJob(resourceService, languageDiscoveryService, jobManager, startupMutex,
                languageServiceMutex);
        job.schedule();
    }


    public void projectOpen(IProject project) {

    }

    public void projectClose(IProject project) {

    }


    public void editorOpen(IEditorInput input, ISourceViewer viewer, String text) {
        processEditor(input, viewer, text);
    }

    public void editorUpdate(IEditorInput input, ISourceViewer viewer, String text) {
        processEditor(input, viewer, text);
    }

    public void editorClose(IEditorInput input) {
        cancelUpdateJobs(input);
    }

    private void processEditor(IEditorInput input, ISourceViewer viewer, String text) {
        cancelUpdateJobs(input);
        final Job job =
            new EditorUpdateJob(resourceService, languageIdentifierService, syntaxService,
                analysisService, categorizerService, stylerService, input, viewer, text);
        job.setRule(startupMutex);
        job.schedule();
    }

    private void cancelUpdateJobs(IEditorInput input) {
        final Job[] existingJobs = jobManager.find(input);
        for(Job job : existingJobs) {
            job.cancel();
        }
    }


    public void build() {

    }
}
