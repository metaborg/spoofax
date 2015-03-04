package org.metaborg.spoofax.eclipse.editor;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.messages.MessageType;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IRegionCategory;
import org.metaborg.spoofax.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.metaborg.spoofax.eclipse.util.StyleUtils;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EditorUpdateJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(EditorUpdateJob.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final IContextService contextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer;
    private final IStylerService<IStrategoTerm, IStrategoTerm> styler;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;

    private final IFileEditorInput input;
    private final IResource eclipseResource;
    private final FileObject resource;
    private final ISourceViewer sourceViewer;
    private final String text;


    public EditorUpdateJob(ILanguageIdentifierService languageIdentifierService, IContextService contextService,
        ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer,
        IStylerService<IStrategoTerm, IStrategoTerm> styler, ParseResultProcessor parseResultProcessor,
        AnalysisResultProcessor analysisResultProcessor, IFileEditorInput input, IResource eclipseResource,
        FileObject resource, ISourceViewer sourceViewer, String text) {
        super("Updating Spoofax editor");
        setSystem(true);
        setPriority(Job.BUILD);

        this.languageIdentifierService = languageIdentifierService;
        this.contextService = contextService;
        this.syntaxService = syntaxService;
        this.analyzer = analyzer;
        this.categorizer = categorizer;
        this.styler = styler;

        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;

        this.input = input;
        this.eclipseResource = eclipseResource;
        this.resource = resource;
        this.sourceViewer = sourceViewer;
        this.text = text;
    }


    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    @Override protected IStatus run(final IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input.getFile());

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            return update(workspace, monitor);
        } catch(SpoofaxException | CoreException e) {
            if(monitor.isCanceled())
                return StatusUtils.cancel();

            try {
                final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
                    @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                        if(workspaceMonitor.isCanceled())
                            return;
                        MarkerUtils.clearAll(eclipseResource);
                        MarkerUtils.createMarker(eclipseResource, MessageFactory.newErrorAtTop(resource,
                            "Failed to update editor", MessageType.INTERNAL_MESSAGE, null));
                    }
                };
                workspace.run(parseMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);
            } catch(CoreException e2) {
                final String message = "Failed to show internal error marker";
                logger.error(message, e2);
                return StatusUtils.silentError(message, e2);
            }

            final String message = String.format("Failed to update editor for %s", resource);
            logger.error(message, e);
            return StatusUtils.silentError(message, e);
        }
    }


    private IStatus update(IWorkspace workspace, final IProgressMonitor monitor) throws SpoofaxException, CoreException {
        final Display display = Display.getDefault();

        // Identify language
        final ILanguage language = languageIdentifierService.identify(resource);
        if(language == null) {
            throw new SpoofaxException("Language could not be identified");
        }

        // Parse
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final ParseResult<IStrategoTerm> parseResult;
        try {
            parseResultProcessor.invalidate(resource);
            parseResult = syntaxService.parse(text, resource, language);
            parseResultProcessor.update(resource, parseResult);
        } catch(ParseException e) {
            parseResultProcessor.error(resource, e);
            throw e;
        }

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        // Update markers atomically using a workspace runnable, to prevent flashing/jumping markers.
        final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearInternal(eclipseResource);
                MarkerUtils.clearParser(eclipseResource);
                for(IMessage message : parseResult.messages) {
                    MarkerUtils.createMarker(eclipseResource, message);
                }
            }
        };
        workspace.run(parseMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);

        // Style
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final Iterable<IRegionCategory<IStrategoTerm>> categories = categorizer.categorize(language, parseResult);
        final Iterable<IRegionStyle<IStrategoTerm>> styles = styler.styleParsed(language, categories);
        final TextPresentation textPresentation = StyleUtils.createTextPresentation(styles, display);
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        display.asyncExec(new Runnable() {
            public void run() {
                if(monitor.isCanceled())
                    return;
                // Also cancel if text presentation is not valid for current text any more.
                if(sourceViewer.getDocument().get().length() != text.length())
                    return;
                sourceViewer.changeTextPresentation(textPresentation, false);
            }
        });

        // Analyze
        try {
            Thread.sleep(600);
        } catch(InterruptedException e) {
            return StatusUtils.error(e);
        }

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final IContext context = contextService.get(resource, language);
        final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult;
        synchronized(context) {
            analysisResultProcessor.invalidate(parseResult.source);
            try {
                analysisResult = analyzer.analyze(Iterables2.singleton(parseResult), context);
            } catch(AnalysisException e) {
                analysisResultProcessor.error(resource, e);
                throw e;
            }
            analysisResultProcessor.update(analysisResult);
        }

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        // Update markers atomically using a workspace runnable, to prevent flashing/jumping markers.
        final IWorkspaceRunnable analysisMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearInternal(eclipseResource);
                MarkerUtils.clearAnalysis(eclipseResource);
                for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                    for(IMessage message : fileResult.messages) {
                        MarkerUtils.createMarker(eclipseResource, message);
                    }
                }
            }
        };
        workspace.run(analysisMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);

        return StatusUtils.success();
    }
}
