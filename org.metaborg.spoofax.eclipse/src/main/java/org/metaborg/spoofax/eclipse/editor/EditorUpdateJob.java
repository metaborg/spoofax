package org.metaborg.spoofax.eclipse.editor;

import java.io.IOException;

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
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IRegionCategory;
import org.metaborg.spoofax.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.ResourceUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.metaborg.spoofax.eclipse.util.StyleUtils;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EditorUpdateJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(EditorUpdateJob.class);
    
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer;
    private final IStylerService<IStrategoTerm, IStrategoTerm> styler;

    private final IFileEditorInput input;
    private final ISourceViewer sourceViewer;
    private final String text;


    public EditorUpdateJob(IEclipseResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        ISyntaxService<IStrategoTerm> syntaxService, IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizer,
        IStylerService<IStrategoTerm, IStrategoTerm> styler, IFileEditorInput input, ISourceViewer sourceViewer,
        String text) {
        super("Updating Spoofax editor");
        this.resourceService = resourceService;
        this.languageIdentifier = languageIdentifier;
        this.syntaxService = syntaxService;
        this.analyzer = analyzer;
        this.categorizer = categorizer;
        this.styler = styler;

        this.input = input;
        this.sourceViewer = sourceViewer;
        this.text = text;
    }


    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    @Override protected IStatus run(final IProgressMonitor monitor) {
        logger.debug("Running editor update job for {}", input.getFile());
        
        try {
            return update(monitor);
        } catch(IOException | CoreException e) {
            e.printStackTrace();
            return StatusUtils.error("Updating editor failed", e);
        }
    }


    private IStatus update(final IProgressMonitor monitor) throws IOException, CoreException {
        final IResource eclipseResource = input.getFile();
        final IResource eclipseLocation = ResourceUtils.getProjectDirectory(eclipseResource);
        final Display display = Display.getDefault();
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        final FileObject resource = resourceService.resolve(eclipseResource);
        final FileObject location = resourceService.resolve(eclipseLocation);
        final ILanguage language = languageIdentifier.identify(resource);

        // Parse
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final ParseResult<IStrategoTerm> parseResult = syntaxService.parse(text, resource, language);
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final IWorkspaceRunnable parseMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
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
        final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
            analyzer.analyze(Iterables2.singleton(parseResult), new SpoofaxContext(language, location));
        if(monitor.isCanceled())
            return StatusUtils.cancel();
        final IWorkspaceRunnable analysisMarkerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                if(workspaceMonitor.isCanceled())
                    return;
                MarkerUtils.clearAnalysis(eclipseResource);
                for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                    for(IMessage message : fileResult.messages()) {
                        MarkerUtils.createMarker(eclipseResource, message);
                    }
                }
            }
        };
        workspace.run(analysisMarkerUpdater, eclipseResource, IWorkspace.AVOID_UPDATE, monitor);

        return StatusUtils.success();
    }
}
