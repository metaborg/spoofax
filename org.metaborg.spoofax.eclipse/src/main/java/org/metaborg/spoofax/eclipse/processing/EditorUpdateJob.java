package org.metaborg.spoofax.eclipse.processing;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.style.ICategorizerService;
import org.metaborg.spoofax.core.style.IRegionCategory;
import org.metaborg.spoofax.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.IStyle;
import org.metaborg.spoofax.core.style.IStylerService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.SpoofaxStatus;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class EditorUpdateJob extends Job {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;

    private final IEditorInput input;
    private final ISourceViewer sourceViewer;
    private final String text;


    public EditorUpdateJob(IEclipseResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService,
        ISyntaxService<IStrategoTerm> syntaxService,
        IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService,
        ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService,
        IStylerService<IStrategoTerm, IStrategoTerm> stylerService, IEditorInput input,
        ISourceViewer sourceViewer, String text) {
        super("Updating Spoofax editor");
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.syntaxService = syntaxService;
        this.analysisService = analysisService;
        this.categorizerService = categorizerService;
        this.stylerService = stylerService;

        this.input = input;
        this.sourceViewer = sourceViewer;
        this.text = text;
    }


    @Override public boolean belongsTo(Object family) {
        return input.equals(family);
    }

    @Override protected IStatus run(final IProgressMonitor monitor) {
        try {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            final IResource eclipseResource = fileInput.getFile();
            final IResource eclipseLocation = getProjectDirectory(eclipseResource);
            final Display display = Display.getDefault();


            final FileObject resource = resourceService.resolve(eclipseResource);
            final FileObject location = resourceService.resolve(eclipseLocation);
            final ILanguage language = languageIdentifierService.identify(resource);


            // Parse
            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
            final ParseResult<IStrategoTerm> parseResult =
                syntaxService.parse(text, resource, language);
            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
            eclipseResource.deleteMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
            for(IMessage message : parseResult.messages) {
                createMarker(eclipseResource, message);
            }

            // Style
            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
            final Iterable<IRegionCategory<IStrategoTerm>> categories =
                categorizerService.categorize(language, parseResult);
            final Iterable<IRegionStyle<IStrategoTerm>> styles =
                stylerService.styleParsed(language, categories);
            final TextPresentation textPresentation = createTextPresentation(styles, display);
            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
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
                return SpoofaxStatus.error(e);
            }

            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
            final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
                analysisService.analyze(Iterables2.singleton(parseResult), new SpoofaxContext(
                    language, location));
            if(monitor.isCanceled())
                return SpoofaxStatus.cancel();
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                for(IMessage message : fileResult.messages()) {
                    createMarker(eclipseResource, message);
                }
            }

            return SpoofaxStatus.success();
        } catch(IOException | CoreException e) {
            e.printStackTrace();
            return SpoofaxStatus.error("Updating editor failed", e);
        }
    }


    private IResource getProjectDirectory(IResource resource) throws IOException {
        final IProject project = resource.getProject();
        if(project != null) {
            return project;
        }

        final IContainer parent = resource.getParent();
        if(parent != null) {
            return parent;
        }

        throw new IOException("Could not get project directory for resource " + resource.toString());
    }


    private IMarker createMarker(IResource resource, IMessage message) throws CoreException {
        final String type = IMarker.PROBLEM;
        final IMarker marker = resource.createMarker(type);
        marker.setAttribute(IMarker.CHAR_START, message.region().startOffset());
        marker.setAttribute(IMarker.CHAR_END, message.region().endOffset() + 1);
        marker.setAttribute(IMarker.LINE_NUMBER, message.region().startRow() + 1);
        marker.setAttribute(IMarker.MESSAGE, message.message());
        marker.setAttribute(IMarker.SEVERITY, severity(message));
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        return marker;
    }

    private int severity(IMessage message) {
        switch(message.severity()) {
            case ERROR:
                return IMarker.SEVERITY_ERROR;
            case WARNING:
                return IMarker.SEVERITY_WARNING;
            case NOTE:
                return IMarker.SEVERITY_INFO;
        }
        return IMarker.SEVERITY_INFO;
    }


    private TextPresentation createTextPresentation(Iterable<IRegionStyle<IStrategoTerm>> styles,
        Display display) {
        final TextPresentation presentation = new TextPresentation();
        for(IRegionStyle<IStrategoTerm> regionStyle : styles) {
            presentation.addStyleRange(createStyleRange(regionStyle, display));
        }
        return presentation;
    }

    private StyleRange createStyleRange(IRegionStyle<IStrategoTerm> regionStyle, Display display) {
        final IStyle style = regionStyle.style();
        final ISourceRegion region = regionStyle.region();

        final StyleRange styleRange = new StyleRange();
        final java.awt.Color foreground = style.color();
        if(foreground != null) {
            styleRange.foreground = createColor(foreground, display);
        }
        final java.awt.Color background = style.backgroundColor();
        if(background != null) {
            styleRange.background = createColor(background, display);
        }
        if(style.bold()) {
            styleRange.fontStyle |= SWT.BOLD;
        }
        if(style.italic()) {
            styleRange.fontStyle |= SWT.ITALIC;
        }
        if(style.underscore()) {
            styleRange.underline = true;
        }

        styleRange.start = region.startOffset();
        styleRange.length = region.endOffset() - region.startOffset() + 1;

        return styleRange;
    }

    private Color createColor(java.awt.Color color, Display display) {
        // GTODO: this color object needs to be disposed manually!
        return new Color(display, color.getRed(), color.getGreen(), color.getBlue());
    }
}
