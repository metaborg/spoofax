package org.metaborg.spoofax.core.transform;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.IResult;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformException;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.spoofax.core.action.TransformAction;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.strategoxt.HybridInterpreter;

import com.google.inject.Inject;

public class StrategoTransformer implements IStrategoTransformer {
    private static final ILogger logger = LoggerUtils.logger(StrategoTransformer.class);

    private final IResourceService resourceService;
    private final IEditorRegistry editorRegistry;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final IStrategoCommon common;


    @Inject public StrategoTransformer(IResourceService resourceService, IEditorRegistry editorRegistry,
        IStrategoRuntimeService strategoRuntimeService, IStrategoCommon common) {
        this.resourceService = resourceService;
        this.editorRegistry = editorRegistry;
        this.strategoRuntimeService = strategoRuntimeService;
        this.common = common;
    }


    @Override public TransformResult<IStrategoTerm, IStrategoTerm> transform(ParseResult<IStrategoTerm> input,
        IContext context, TransformActionContribution actionContribution) throws TransformException {
        return transform(input, context, actionContribution, input.source, input.result);
    }

    @Override public TransformResult<IStrategoTerm, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> input, IContext context,
        TransformActionContribution actionContribution) throws TransformException {
        return transform(input, context, actionContribution, input.source, input.result);
    }

    private TransformResult<IStrategoTerm, IStrategoTerm> transform(IResult<IStrategoTerm> input, IContext context,
        TransformActionContribution actionContribution, FileObject source, IStrategoTerm term)
        throws TransformException {
        final FileObject location = context.location();
        final ILanguageComponent component = actionContribution.contributor;
        final TransformAction action = action(actionContribution.action);

        // Get input term
        final IStrategoTerm inputTerm;
        try {
            inputTerm = common.builderInputTerm(term, source, location);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly; cannot create input term", e);
        }

        // Get Stratego runtime
        final HybridInterpreter runtime;
        try {
            runtime = strategoRuntimeService.runtime(component, context);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly; cannot get Stratego interpreter", e);
        }

        // Transform
        logger.debug("Transforming {} with '{}'", source, action.name);
        final Timer timer = new Timer(true);
        final IStrategoTerm outputTerm;
        try {
            outputTerm = common.invoke(runtime, inputTerm, action.strategy);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed unexpectedly", e);
        }
        final long duration = timer.stop();
        if(outputTerm == null) {
            final String message = logger.format("Invoking Stratego strategy {} failed", action.strategy);
            throw new TransformException(message);
        }

        // Write to file
        final IStrategoTerm resultTerm;
        final FileObject outputFile;
        if(outputTerm.getSubtermCount() == 2 && (outputTerm instanceof IStrategoTuple)) {
            final IStrategoTerm resourceTerm = outputTerm.getSubterm(0);
            resultTerm = outputTerm.getSubterm(1);
            if(!(resourceTerm instanceof IStrategoString)) {
                outputFile = null;
                logger.error("First term of result tuple {} is not a string, cannot write output file");
            } else {
                final String resourceString = Tools.asJavaString(resourceTerm);
                final String resultContents = common.toString(resultTerm);

                outputFile = resourceService.resolve(location, resourceString);
                try(OutputStream stream = outputFile.getContent().getOutputStream()) {
                    IOUtils.write(resultContents, stream);
                } catch(IOException e) {
                    logger.error("Error occurred while writing output file", e);
                }
            }
        } else {
            resultTerm = outputTerm;
            outputFile = null;
        }

        // Open editor
        if(outputFile != null && action.flags.openEditor) {
            editorRegistry.open(context.language(), outputFile);
        }

        // Return result
        return new TransformResult<>(resultTerm, source, outputFile, context, duration, actionContribution, input);
    }

    private TransformAction action(ITransformAction action) throws TransformException {
        if(!(action instanceof TransformAction)) {
            final String message = logger.format("Action {} is not a Stratego transformation action", action);
            throw new TransformException(message);
        }
        return (TransformAction) action;
    }
}
