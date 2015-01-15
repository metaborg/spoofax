package org.metaborg.spoofax.eclipse.editor;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class SpoofaxReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService;
    private final SpoofaxEditor editor;

    private IDocument document;
    private IProgressMonitor monitor;


    public SpoofaxReconcilingStrategy(SpoofaxEditor editor) {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.syntaxService =
            injector.getInstance(Key.get(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}));
        this.analysisService =
            injector.getInstance(Key
                .get(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
        this.editor = editor;
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
            final String input = document.get();
            final IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
            final IResource eclipseResource = editorInput.getFile();
            final FileObject resource = resourceService.resolve(eclipseResource);
            final ILanguage language = languageIdentifierService.identify(resource);
            final ParseResult<IStrategoTerm> parseResult = syntaxService.parse(input, resource, language);
            // final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
            // analysisService.analyze(Iterables2.singleton(parseResult), language);

            eclipseResource.deleteMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);

            for(IMessage message : parseResult.messages) {
                createMarker(eclipseResource, message);
            }

            // for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : analysisResult.fileResults) {
            // for(IMessage message : fileResult.messages()) {
            // createMarker(eclipseResource, message);
            // }
            // }
        } catch(IOException | CoreException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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
}
