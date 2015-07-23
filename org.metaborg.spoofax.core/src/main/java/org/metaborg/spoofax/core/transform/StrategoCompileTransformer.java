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
import org.metaborg.spoofax.core.transform.compile.CompilerFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

import fj.P;
import fj.P2;

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
        final P2<String, ILanguageComponent> tuple = strategyName(context.language());
        final String strategyName = tuple._1();
        final ILanguageComponent component = tuple._2();

        final FileObject resource = parseResult.source;
        final IStrategoTerm inputTerm;
        try {
            inputTerm = transformer.builderInputTerm(parseResult.result, resource, context.location());
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }
        logger.debug("Compiling parse result of {}", resource);
        return transformer.transform(component, context, parseResult, strategyName, inputTerm, resource);
    }

    @Override public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> transform(
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult, IContext context, ITransformerGoal goal)
        throws TransformerException {
        final P2<String, ILanguageComponent> tuple = strategyName(context.language());
        final String strategyName = tuple._1();
        final ILanguageComponent component = tuple._2();

        final FileObject resource = analysisResult.source;
        final IStrategoTerm inputTerm;
        try {
            inputTerm = transformer.builderInputTerm(analysisResult.result, resource, context.location());
        } catch(MetaborgException e) {
            throw new TransformerException("Cannot create input term", e);
        }
        logger.debug("Compiling analysis result of {}", resource);
        return transformer.transform(component, context, analysisResult, strategyName, inputTerm, resource);
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
