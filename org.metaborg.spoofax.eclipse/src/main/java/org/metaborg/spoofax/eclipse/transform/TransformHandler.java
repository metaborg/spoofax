package org.metaborg.spoofax.eclipse.transform;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.editor.ISpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditorListener;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class TransformHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService langaugeIdentifierService;
    private final IContextService contextService;
    private final ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;

    private final ISpoofaxEditorListener latestEditorListener;


    public TransformHandler() {
        final Injector injector = SpoofaxPlugin.injector();

        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.langaugeIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.contextService = injector.getInstance(IContextService.class);
        this.transformer =
            injector.getInstance(Key
                .get(new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}));

        this.parseResultProcessor = injector.getInstance(ParseResultProcessor.class);
        this.analysisResultProcessor = injector.getInstance(AnalysisResultProcessor.class);

        this.latestEditorListener = injector.getInstance(SpoofaxEditorListener.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final SpoofaxEditor latestEditor = latestEditorListener.previousEditor();
        final String actionName = event.getParameter(TransformMenuContribution.actionNameParam);
        final Job transformJob =
            new TransformJob(resourceService, langaugeIdentifierService, contextService, transformer,
                parseResultProcessor, analysisResultProcessor, latestEditor, actionName);
        transformJob.schedule();

        return null;
    }
}
