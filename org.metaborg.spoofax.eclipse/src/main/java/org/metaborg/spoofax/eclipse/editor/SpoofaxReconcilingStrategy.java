package org.metaborg.spoofax.eclipse.editor;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
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
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class SpoofaxReconcilingStrategy implements IReconcilingStrategy,
    IReconcilingStrategyExtension {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final ICategorizerService<IStrategoTerm, IStrategoTerm> categorizerService;
    private final IStylerService<IStrategoTerm, IStrategoTerm> stylerService;

    private final SpoofaxEditor editor;
    private final ISourceViewer sourceViewer;

    private IDocument document;
    private IProgressMonitor monitor;


    public SpoofaxReconcilingStrategy(SpoofaxEditor editor, ISourceViewer sourceViewer) {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.syntaxService =
            injector.getInstance(Key.get(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}));
        this.analysisService =
            injector.getInstance(Key
                .get(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
        this.categorizerService =
            injector.getInstance(Key
                .get(new TypeLiteral<ICategorizerService<IStrategoTerm, IStrategoTerm>>() {}));
        this.stylerService =
            injector.getInstance(Key
                .get(new TypeLiteral<IStylerService<IStrategoTerm, IStrategoTerm>>() {}));

        this.editor = editor;
        this.sourceViewer = sourceViewer;
    }

    @Override public void setDocument(IDocument document) {
        this.document = document;
    }

    @Override public void setProgressMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override public void initialReconcile() {
        process();
    }

    @Override public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
        process();
    }

    @Override public void reconcile(IRegion partition) {
        process();
    }

    private void process() {
        try {
            final IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
            final String input = document.get();
            final IResource eclipseResource = editorInput.getFile();
            final IResource eclipseLocation = getProjectDirectory(eclipseResource);
            final Display display = Display.getDefault();


            final FileObject resource = resourceService.resolve(eclipseResource);
            final FileObject location = resourceService.resolve(eclipseLocation);
            final ILanguage language = languageIdentifierService.identify(resource);


            // Parse
            final ParseResult<IStrategoTerm> parseResult =
                syntaxService.parse(input, resource, language);
            eclipseResource.deleteMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
            for(IMessage message : parseResult.messages) {
                createMarker(eclipseResource, message);
            }

            // Style
            final Iterable<IRegionCategory<IStrategoTerm>> categories =
                categorizerService.categorize(language, parseResult);
            final Iterable<IRegionStyle<IStrategoTerm>> styles =
                stylerService.styleParsed(language, categories);
            display.asyncExec(new Runnable() {
                public void run() {
                    final TextPresentation textPresentation =
                        createTextPresentation(styles, display);
                    sourceViewer.changeTextPresentation(textPresentation, false);
                }
            });

            // Analyze
            final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
                analysisService.analyze(Iterables2.singleton(parseResult), new SpoofaxContext(
                    language, location));
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
                for(IMessage message : fileResult.messages()) {
                    createMarker(eclipseResource, message);
                }
            }
        } catch(IOException | CoreException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
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

    private StyleRange createStyleRange(IRegionStyle regionStyle, Display display) {
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
