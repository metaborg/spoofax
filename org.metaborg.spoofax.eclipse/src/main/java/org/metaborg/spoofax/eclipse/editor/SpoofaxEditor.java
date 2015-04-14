package org.metaborg.spoofax.eclipse.editor;

import java.awt.Color;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.editors.text.TextEditor;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.job.GlobalSchedulingRules;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.StyleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class SpoofaxEditor extends TextEditor {
    private static final Logger logger = LoggerFactory.getLogger(SpoofaxEditor.class);

    public static final String id = SpoofaxPlugin.id + ".editor";

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;
    private final IDialectService dialectService;
    private final IContextService contextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;
    private final GlobalSchedulingRules globalRules;

    private final IJobManager jobManager;

    private final IPropertyListener editorInputChangedListener;
    private final PresentationMerger presentationMerger;

    private IEditorInput input;
    private IDocument document;
    private ISourceViewer sourceViewer;
    private ITextViewerExtension4 textViewerExt;
    private DocumentListener documentListener;


    public SpoofaxEditor() {
        super();

        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageIdentifier = injector.getInstance(ILanguageIdentifierService.class);
        this.dialectService = injector.getInstance(IDialectService.class);
        this.contextService = injector.getInstance(IContextService.class);
        this.syntaxService = injector.getInstance(Key.get(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}));
        this.analysisService =
            injector.getInstance(Key.get(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
        this.categorizerService =
            injector.getInstance(Key.get(new TypeLiteral<ICategorizerService<IStrategoTerm, IStrategoTerm>>() {}));
        this.stylerService =
            injector.getInstance(Key.get(new TypeLiteral<IStylerService<IStrategoTerm, IStrategoTerm>>() {}));

        this.parseResultProcessor = injector.getInstance(ParseResultProcessor.class);
        this.analysisResultProcessor = injector.getInstance(AnalysisResultProcessor.class);
        this.globalRules = injector.getInstance(GlobalSchedulingRules.class);

        this.jobManager = Job.getJobManager();

        this.editorInputChangedListener = new EditorInputChangedListener();
        this.presentationMerger = new PresentationMerger();

        setDocumentProvider(new SpoofaxDocumentProvider());
    }


    public IDocument currentDocument() {
        if(!checkInitialized()) {
            return null;
        }
        return document;
    }

    public void enable() {
        if(!checkInitialized()) {
            return;
        }
        if(documentListener == null) {
            logger.debug("Enabling editor for {}", input);
            documentListener = new DocumentListener();
            document.addDocumentListener(documentListener);
            scheduleJob(true);
        }
    }

    public void disable() {
        if(!checkInitialized()) {
            return;
        }
        if(documentListener != null) {
            logger.debug("Disabling editor for {}", input);
            document.removeDocumentListener(documentListener);
            documentListener = null;

            final Display display = Display.getDefault();
            final TextPresentation grayPresentation =
                StyleUtils.createTextPresentation(Color.BLACK, document.getLength(), display);
            presentationMerger.invalidate();
            display.asyncExec(new Runnable() {
                @Override public void run() {
                    sourceViewer.changeTextPresentation(grayPresentation, true);
                }
            });
        }
    }

    public void forceUpdate() {
        if(!checkInitialized()) {
            return;
        }
        logger.debug("Force updating editor for {}", input);
        scheduleJob(true);
    }


    @Override protected void initializeEditor() {
        super.initializeEditor();

        setSourceViewerConfiguration(new SpoofaxSourceViewerConfiguration());
    }

    @Override protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        sourceViewer = super.createSourceViewer(parent, ruler, styles);
        textViewerExt = (ITextViewerExtension4) sourceViewer;

        // Store current input and document so we have access to them when the editor input changes.
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);
        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        // Register for changes in the editor input, to handle renaming or moving of resources of open editors.
        this.addPropertyListener(editorInputChangedListener);

        // Register for changes in text presentation, to merge our text presentation with presentations from other
        // sources, such as marker annotations.
        textViewerExt.addTextPresentationListener(presentationMerger);

        scheduleJob(true);

        return sourceViewer;
    }

    @Override public void dispose() {
        cancelJobs(input);

        if(documentListener != null) {
            document.removeDocumentListener(documentListener);
        }
        this.removePropertyListener(editorInputChangedListener);
        textViewerExt.removeTextPresentationListener(presentationMerger);

        input = null;
        document = null;
        sourceViewer = null;
        textViewerExt = null;
        documentListener = null;

        super.dispose();
    }


    private boolean checkInitialized() {
        if(input == null || document == null || sourceViewer == null) {
            logger.error("Attempted to use before it was initialized");
            return false;
        }
        return true;
    }

    private void scheduleJob(boolean instantaneous) {
        if(!checkInitialized()) {
            return;
        }

        cancelJobs(input);

        // THREADING: invalidate text styling here on the main thread (instead of in the editor update job), to prevent
        // race conditions.
        presentationMerger.invalidate();

        final IFileEditorInput fileInput = (IFileEditorInput) input;
        final IFile file = fileInput.getFile();
        final FileObject resource = resourceService.resolve(file);
        final Job job =
            new EditorUpdateJob(languageIdentifier, dialectService, contextService, syntaxService, analysisService,
                categorizerService, stylerService, parseResultProcessor, analysisResultProcessor, fileInput, file,
                resource, sourceViewer, document.get(), presentationMerger, instantaneous);
        job.setRule(new MultiRule(new ISchedulingRule[] { globalRules.startupReadLock(), file }));
        job.schedule(instantaneous ? 0 : 100);
    }

    private void cancelJobs(IEditorInput specificInput) {
        logger.trace("Cancelling editor update jobs for {}", specificInput);
        final Job[] existingJobs = jobManager.find(specificInput);
        for(Job job : existingJobs) {
            job.cancel();
        }
    }

    private void editorInputChanged() {
        final IEditorInput oldInput = input;
        final IDocument oldDocument = document;

        logger.debug("Editor input changed from {} to {}", oldInput, input);

        // Unregister old document listener and register a new one, because the input changed which also changes the
        // document.
        if(documentListener != null) {
            oldDocument.removeDocumentListener(documentListener);
        }
        input = getEditorInput();
        document = getDocumentProvider().getDocument(input);
        documentListener = new DocumentListener();
        document.addDocumentListener(documentListener);

        cancelJobs(oldInput);
        scheduleJob(true);
    }


    private final class DocumentListener implements IDocumentListener {
        @Override public void documentAboutToBeChanged(DocumentEvent event) {

        }

        @Override public void documentChanged(DocumentEvent event) {
            scheduleJob(false);
        }
    }

    private final class EditorInputChangedListener implements IPropertyListener {
        @Override public void propertyChanged(Object source, int propId) {
            if(propId == IEditorPart.PROP_INPUT) {
                editorInputChanged();
            }
        }
    }
}