package org.metaborg.spoofax.eclipse.transform;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.processing.ISpoofaxAnalysisResultRequester;
import org.metaborg.spoofax.core.processing.ISpoofaxParseResultRequester;
import org.metaborg.spoofax.core.transform.stratego.IStrategoTransformer;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.editor.IEclipseEditor;
import org.metaborg.spoofax.eclipse.editor.IEclipseEditorRegistry;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Injector;

public class TransformHandler extends AbstractHandler {
    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService langaugeIdentifierService;
    private final IContextService contextService;
    private final IStrategoTransformer transformer;

    private final ISpoofaxParseResultRequester parseResultRequester;
    private final ISpoofaxAnalysisResultRequester analysisResultRequester;

    private final IEclipseEditorRegistry latestEditorListener;


    public TransformHandler() {
        final Injector injector = SpoofaxPlugin.injector();

        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.langaugeIdentifierService = injector.getInstance(ILanguageIdentifierService.class);
        this.contextService = injector.getInstance(IContextService.class);
        this.transformer = injector.getInstance(IStrategoTransformer.class);

        this.parseResultRequester = injector.getInstance(ISpoofaxParseResultRequester.class);
        this.analysisResultRequester = injector.getInstance(ISpoofaxAnalysisResultRequester.class);

        this.latestEditorListener = injector.getInstance(IEclipseEditorRegistry.class);
    }


    @Override public Object execute(ExecutionEvent event) throws ExecutionException {
        final IEclipseEditor latestEditor = latestEditorListener.previousEditor();
        final String actionName = event.getParameter(TransformMenuContribution.actionNameParam);
        final Job transformJob =
            new TransformJob<IStrategoTerm, IStrategoTerm, IStrategoTerm>(resourceService, langaugeIdentifierService,
                contextService, transformer, parseResultRequester, analysisResultRequester, latestEditor, actionName);
        transformJob.schedule();

        return null;
    }
}
