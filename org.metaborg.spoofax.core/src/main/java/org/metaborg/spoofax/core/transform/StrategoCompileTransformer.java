package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.CompileGoal;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.spoofax.core.stratego.StrategoCommon;
import org.metaborg.spoofax.core.transform.compile.CompilerFacet;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

import fj.P;
import fj.P2;

/**
 * Transformer executor for the {@link CompileGoal} and {@link CompilerFacet}.
 */
public class StrategoCompileTransformer implements IStrategoTransformerExecutor {
    private static final ILogger logger = LoggerUtils.logger(StrategoCompileTransformer.class);

    private final StrategoCommon transformer;


    @Inject public StrategoCompileTransformer(StrategoCommon transformer) {
        this.transformer = transformer;
    }


    @Override public TransformResult<ParseResult<IStrategoTerm>, IStrategoTerm> transform(
        ParseResult<IStrategoTerm> parseResult, IContext context, ITransformerGoal goal) throws TransformerException {
        final FileObject resource = parseResult.source;
        if(parseResult.result == null) {
            final String message = logger.format("Cannot transform {}, parsed AST is null", resource);
            throw new TransformerException(message);
        }

        final P2<String, ILanguageComponent> tuple = strategyName(context.language());
        final String strategyName = tuple._1();
        final ILanguageComponent component = tuple._2();

        final IStrategoTerm inputTerm;
        try {
            inputTerm = transformer.builderInputTerm(parseResult.result, resource, context.location());
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }

        try {
            logger.debug("Compiling parse result of {}", resource);
            return transformer.transform(component, context, parseResult, strategyName, inputTerm, resource);
        } catch(MetaborgException e) {
            throw new TransformerException("Transformation failed", e);
        }
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final FileObject resource = analysisResult.source;
        if(analysisResult.result == null) {
            final String message = logger.format("Cannot transform {}, analyzed AST is null", resource);
            throw new TransformerException(message);
        }

        final P2<String, ILanguageComponent> tuple = strategyName(context.language());
        final String strategyName = tuple._1();
        final ILanguageComponent component = tuple._2();

        final IStrategoTerm inputTerm;
        try {
            inputTerm = transformer.builderInputTerm(analysisResult.result, resource, context.location());
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }

        try {
            logger.debug("Compiling analysis result of {}", resource);
            return transformer.transform(component, context, analysisResult, strategyName, inputTerm, resource);
        } catch(MetaborgException e) {
            throw new TransformerException("Transformation failed", e);
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


    private P2<String, ILanguageComponent> strategyName(ILanguageImpl language) throws TransformerException {
        final FacetContribution<CompilerFacet> facetContribution = language.facetContribution(CompilerFacet.class);
        if(facetContribution == null) {
            final String message = String.format("No compiler facet found for %s", language);
            logger.error(message);
            throw new TransformerException(message);
        }
        final CompilerFacet facet = facetContribution.facet;

        final String strategyName = facet.strategyName;
        if(strategyName == null) {
            final String message = String.format("No compiler strategy found for %s", language);
            logger.error(message);
            throw new TransformerException(message);
        }
        return P.p(strategyName, facetContribution.contributor);
    }
}
