package org.metaborg.spoofax.core.analysis.legacy;

import java.io.File;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisCommon;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisFacet;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoCommon;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fj.P;
import fj.P2;

/**
 * Analyzer for legacy Stratego projects. Calls the analysis strategy for each input.
 */
public class StrategoAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "stratego";

    private static final Logger logger = LoggerFactory.getLogger(StrategoAnalyzer.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final StrategoCommon common;


    @Inject public StrategoAnalyzer(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService, StrategoCommon common) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.common = common;
    }


    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ParseResult<IStrategoTerm>> inputs,
        IContext context) throws AnalysisException {
        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<SpoofaxAnalysisFacet> facetContribution =
            language.facetContribution(SpoofaxAnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context);
        }
        final SpoofaxAnalysisFacet facet = facetContribution.facet;

        final HybridInterpreter interpreter;
        try {
            interpreter = runtimeService.runtime(facetContribution.contributor, context);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego interpreter", e);
        }

        return analyze(inputs, context, interpreter, facet.strategyName, termFactory);
    }

    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ParseResult<IStrategoTerm>> inputs,
        IContext context, HybridInterpreter interpreter, String analysisStrategy, ITermFactory termFactory)
        throws AnalysisException {
        final FileObject contextLocation = context.location();
        final File localContextLocation;
        try {
            localContextLocation = resourceService.localFile(contextLocation);
        } catch(MetaborgRuntimeException e) {
            final String message = String.format("Context location %s does not exist, cannot analyze", contextLocation);
            logger.error(message);
            throw new AnalysisException(context, message, e);
        }

        logger.trace("Creating input terms for analysis (3-tuple terms)");
        final Collection<P2<ParseResult<IStrategoTerm>, IStrategoTuple>> analysisInputs = Lists.newLinkedList();
        for(ParseResult<IStrategoTerm> input : inputs) {
            if(input.result == null) {
                logger.warn("Input result for {} is null, cannot analyze it", input.source);
                continue;
            }

            final FileObject resource = input.source;
            final File localResource;
            try {
                if(input.source.exists()) {
                    localResource = resourceService.localFile(resource);
                } else {
                    localResource = resourceService.localPath(resource);
                }
                if(localResource == null) {
                    logger.error(
                        "Input {} does not exist, and cannot reside on the local file system, cannot analyze it",
                        resource);
                    continue;
                }
            } catch(FileSystemException e) {
                logger.error("Cannot determine if input {} exists, cannot analyze it", resource);
                continue;
            }

            final IStrategoString path = common.localResourceTerm(localResource, localContextLocation);
            final IStrategoString contextPath = common.localLocationTerm(localContextLocation);
            analysisInputs.add(P.p(input, termFactory.makeTuple(input.result, path, contextPath)));
        }

        final Collection<AnalysisFileResult<IStrategoTerm, IStrategoTerm>> results = Lists.newLinkedList();
        for(P2<ParseResult<IStrategoTerm>, IStrategoTuple> input : analysisInputs) {
            final ParseResult<IStrategoTerm> parseResult = input._1();
            final FileObject resource = parseResult.source;
            final IStrategoTuple inputTerm = input._2();
            try {
                logger.trace("Analysing {}", resource);
                final IStrategoTerm resultTerm = common.invoke(interpreter, inputTerm, analysisStrategy);
                if(resultTerm == null) {
                    logger.trace("Analysis for {} failed", resource);
                    results.add(result(SpoofaxAnalysisCommon.analysisFailedMessage(interpreter), parseResult, context,
                        null));
                } else if(!(resultTerm instanceof IStrategoTuple)) {
                    logger.trace("Analysis for {} has unexpected result, not a tuple", resource);
                    results.add(result(String.format("Unexpected results from analysis {}", resultTerm), parseResult,
                        context, null));
                } else if(resultTerm.getSubtermCount() == 4) {
                    logger.trace("Analysis for {} done", resource);
                    results.add(result(resultTerm, parseResult, context));
                } else if(resultTerm.getSubtermCount() == 3) {
                    logger.trace("Analysis for {} done", resource);
                    results.add(resultNoAst(resultTerm, parseResult, context));
                } else {
                    logger.trace(
                        "Analysis for {} has unexpected result, tuple with more than 4 or less than 2 elements",
                        resource);
                    results.add(result(String.format("Unexpected results from analysis {}", resultTerm), parseResult,
                        context, null));
                }
            } catch(MetaborgException e) {
                logger.trace("Analysis for {} failed", resource);
                results.add(result(SpoofaxAnalysisCommon.analysisFailedMessage(interpreter), parseResult, context, e));
            }
        }

        return new AnalysisResult<IStrategoTerm, IStrategoTerm>(context, results);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> result(IStrategoTerm result,
        ParseResult<IStrategoTerm> parseResult, IContext context) {
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(1)));
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(2)));
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(3)));
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(result.getSubterm(0), source, context, messages,
            parseResult);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> resultNoAst(IStrategoTerm result,
        ParseResult<IStrategoTerm> parseResult, IContext context) {
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(0)));
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(1)));
        messages.addAll(SpoofaxAnalysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(2)));
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(null, source, context, messages, parseResult);
    }

    private AnalysisFileResult<IStrategoTerm, IStrategoTerm> result(String errorString,
        ParseResult<IStrategoTerm> parseResult, IContext context, Throwable e) {
        if(e != null) {
            logger.error(errorString, e);
        } else {
            logger.error(errorString);
        }
        final FileObject source = parseResult.source;
        final IMessage message = MessageFactory.newAnalysisErrorAtTop(source, errorString, e);
        return new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(null, source, context,
            Iterables2.singleton(message), parseResult);
    }
}
