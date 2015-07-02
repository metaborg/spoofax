package org.metaborg.spoofax.core.transform.stratego;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.SpoofaxException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.transform.stratego.compile.CompilerFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

/**
 * Transformer executor for the {@link CompileGoal} and {@link CompilerFacet}.
 */
public class StrategoCompileTransformer implements IStrategoTransformerExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StrategoCompileTransformer.class);

    private final StrategoTransformerCommon transformer;


    @Inject public StrategoCompileTransformer(StrategoTransformerCommon transformer) {
        this.transformer = transformer;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transform(
        ParseResult<IStrategoTerm> parseResult, IContext context, ITransformerGoal goal) throws TransformerException {
        final String strategyName = strategyName(context.language());
        final FileObject resource = parseResult.source;
        try {
            final IStrategoTerm inputTerm = transformer.builderInputTerm(parseResult.result, resource, context.location());
            logger.debug("Compiling parse result of {}", resource);
            return transformer.transform(context, parseResult, strategyName, inputTerm, resource);
        } catch(SpoofaxException e) {
            throw new TransformerException("Cannot create input term", e);
        }
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final String strategyName = strategyName(context.language());
        final FileObject resource = analysisResult.source;
        try {
            final IStrategoTerm inputTerm =
                transformer.builderInputTerm(analysisResult.result, resource, context.location());
            logger.debug("Compiling analysis result of {}", resource);
            return transformer.transform(context, analysisResult, strategyName, inputTerm, resource);
        } catch(SpoofaxException e) {
            throw new TransformerException("Cannot create input term", e);
        }
    }

    @Override public boolean available(ITransformerGoal goal, IContext context) {
        final CompilerFacet facet = context.language().facet(CompilerFacet.class);
        if(facet == null) {
            return false;
        }
        if(facet.strategyName == null) {
            return false;
        }
        return true;
    }


    private String strategyName(ILanguage language) throws TransformerException {
        final CompilerFacet facet = language.facet(CompilerFacet.class);
        if(facet == null) {
            final String message = String.format("No compiler facet found for %s", language);
            logger.error(message);
            throw new TransformerException(message);
        }

        final String strategyName = facet.strategyName;
        if(strategyName == null) {
            final String message = String.format("No compiler strategy found for %s", language);
            logger.error(message);
            throw new TransformerException(message);
        }
        return strategyName;
    }
}
