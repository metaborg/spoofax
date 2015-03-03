package org.metaborg.spoofax.eclipse.transform;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.metaborg.spoofax.core.transform.stratego.Action;
import org.metaborg.spoofax.core.transform.stratego.MenusFacet;
import org.metaborg.spoofax.core.transform.stratego.StrategoTransformResultProcessor;
import org.metaborg.spoofax.eclipse.editor.SpoofaxEditor;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(TransformJob.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageIdentifierService langaugeIdentifierService;
    private final IContextService contextService;
    private final ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;

    private final SpoofaxEditor editor;
    private final String actionName;


    public TransformJob(IEclipseResourceService resourceService, ILanguageIdentifierService langaugeIdentifierService,
        IContextService contextService, ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer,
        ParseResultProcessor parseResultProcessor, AnalysisResultProcessor analysisResultProcessor,
        SpoofaxEditor editor, String actionName) {
        super("Transforming file");

        this.resourceService = resourceService;
        this.langaugeIdentifierService = langaugeIdentifierService;
        this.contextService = contextService;
        this.transformer = transformer;

        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;

        this.editor = editor;
        this.actionName = actionName;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        final IEditorInput input = editor.getEditorInput();
        final String text = editor.currentDocument().get();
        final FileObject resource = resourceService.resolve(input);

        if(resource == null) {
            final String message = String.format("Transformation failed, input %s cannot be resolved", input);
            logger.error(message);
            return StatusUtils.error(message);
        }

        final ILanguage language = langaugeIdentifierService.identify(resource);
        if(language == null) {
            final String message = String.format("Transformation failed, language of %s cannot be identified", resource);
            logger.error(message);
            return StatusUtils.error(message);
        }

        final MenusFacet facet = language.facet(MenusFacet.class);
        if(facet == null) {
            final String message = String.format("Transformation failed, %s does not have a menus facet", language);
            logger.error(message);
            return StatusUtils.error(message);
        }

        final Action action = facet.action(actionName);
        if(action == null) {
            final String message =
                String.format("Transformation failed, %s does not have an action named %s", language, actionName);
            logger.error(message);
            return StatusUtils.error(message);
        }

        try {
            return transform(monitor, resource, language, action, text);
        } catch(IOException | ContextException | TransformerException e) {
            final String message = String.format("Transformation failed for %s", resource);
            logger.error(message, e);
            return StatusUtils.error(message, e);
        }
    }

    private IStatus transform(IProgressMonitor monitor, FileObject resource, ILanguage language, Action action,
        String text) throws IOException, ContextException, TransformerException {
        if(monitor.isCanceled())
            return StatusUtils.cancel();

        final IContext context = contextService.get(resource, language);
        final TransformResult<?, IStrategoTerm> transformResult;
        if(action.flags.parsed) {
            final ParseResult<IStrategoTerm> result =
                parseResultProcessor.request(resource, language, text).toBlocking().single();
            transformResult = transformer.transformParsed(result, context, action.name);
        } else {
            final AnalysisFileResult<IStrategoTerm, IStrategoTerm> result =
                analysisResultProcessor.request(resource, context, text).toBlocking().single();
            transformResult = transformer.transformAnalyzed(result, context, action.name);
        }

        if(monitor.isCanceled())
            return StatusUtils.cancel();
        // GTODO: don't depend on StrategoTransformResultProcessor, should be abstracted out to support different
        // analyzers.
        final FileObject writtenResource = StrategoTransformResultProcessor.writeFile(transformResult.result, context);
        if(action.flags.openEditor && writtenResource != null) {
            final IResource writtenEclipseResource = resourceService.unresolve(writtenResource);
            if(writtenEclipseResource instanceof IFile) {
                final IFile file = (IFile) writtenEclipseResource;
                // Run in the UI thread because we need to get the active workbench window and page.
                final Display display = Display.getDefault();
                display.asyncExec(new Runnable() {
                    @Override public void run() {
                        final IWorkbenchPage page =
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        try {
                            IDE.openEditor(page, file);
                        } catch(PartInitException e) {
                            logger.error("Cannot open editor", e);
                        }
                    }
                });
            }
        }

        return StatusUtils.success();
    }
}
