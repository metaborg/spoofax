package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.ITransformAction;
import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.TransformException;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.spoofax.core.action.TransformAction;
import org.metaborg.spoofax.core.stratego.StrategoCommon;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class StrategoTransformer implements IStrategoTransformer {
    private static final ILogger logger = LoggerUtils.logger(StrategoTransformer.class);

    private final IEditorRegistry editorRegistry;
    private final StrategoCommon common;


    @Inject public StrategoTransformer(IEditorRegistry editorRegistry, StrategoCommon common) {
        this.editorRegistry = editorRegistry;
        this.common = common;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transform(
        ParseResult<IStrategoTerm> input, IContext context, TransformActionContribution actionContribution)
        throws TransformException {
        return transform(input, context, actionContribution, input.source, input.result);
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> input, IContext context,
        TransformActionContribution actionContribution) throws TransformException {
        return transform(input, context, actionContribution, input.source, input.result);
    }

    private <V> TransformResult<V, IStrategoTerm> transform(V input, IContext context,
        TransformActionContribution actionContribution, FileObject resource, IStrategoTerm term)
        throws TransformException {
        final ILanguageComponent component = actionContribution.contributor;
        final TransformAction action = action(actionContribution.action);

        final IStrategoTerm inputTerm;
        try {
            inputTerm = common.builderInputTerm(term, resource, context.location());
        } catch(MetaborgException e) {
            throw new TransformException("Cannot create input term", e);
        }

        final TransformResult<V, IStrategoTerm> result;
        try {
            logger.debug("Transforming {} with '{}'", resource, action.name);
            result = common.transform(component, context, input, action.strategy, inputTerm, resource);
        } catch(MetaborgException e) {
            throw new TransformException("Transformation failed", e);
        }

        final FileObject outputFile = common.builderWriteResult(result.result, context.location());
        
        if(outputFile != null && action.flags.openEditor) {
            editorRegistry.open(outputFile);
        }

        return result;
    }

    private TransformAction action(ITransformAction action) throws TransformException {
        if(!(action instanceof TransformAction)) {
            final String message = logger.format("Action {} is not a Stratego transformation action", action);
            throw new TransformException(message);
        }
        return (TransformAction) action;
    }
}
