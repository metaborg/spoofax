package org.metaborg.spoofax.core.analysis.legacy;

import java.io.File;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
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

    private static final ILogger logger = LoggerUtils.logger(StrategoAnalyzer.class);

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final IStrategoCommon strategoCommon;
    private final AnalysisCommon analysisCommon;


    @Inject public StrategoAnalyzer(IResourceService resourceService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon, AnalysisCommon analysisCommon) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.analysisCommon = analysisCommon;
    }


    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ISpoofaxParseUnit> inputs,
        IContext context) throws AnalysisException {
        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<AnalysisFacet> facetContribution = language.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", language);
            return new AnalysisResult<>(context);
        }
        final AnalysisFacet facet = facetContribution.facet;

        final HybridInterpreter interpreter;
        try {
            interpreter = runtimeService.runtime(facetContribution.contributor, context);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego interpreter", e);
        }

        return analyze(inputs, context, interpreter, facet.strategyName, termFactory);
    }

    private AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ISpoofaxParseUnit> inputs,
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
        final Collection<P2<ISpoofaxParseUnit, IStrategoTuple>> analysisInputs = Lists.newLinkedList();
        for(ISpoofaxParseUnit input : inputs) {
            if(input.result == null) {
                logger.warn("Parse result for {} is null, cannot analyze", input.source);
                continue;
            }

            final IStrategoString path;
            final FileObject resource = input.source;
            if(resource != null) {
                final File localResource;
                try {
                    if(resource.exists()) {
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
                path = strategoCommon.localResourceTerm(localResource, localContextLocation);
            } else {
                logger.debug("Parse result has no source, using 'null' as path");
                path = termFactory.makeString("null");
            }
            final IStrategoString contextPath = strategoCommon.localLocationTerm(localContextLocation);
            analysisInputs.add(P.p(input, termFactory.makeTuple(input.result, path, contextPath)));
        }

        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newLinkedList();
        for(P2<ISpoofaxParseUnit, IStrategoTuple> input : analysisInputs) {
            final ISpoofaxParseUnit parseResult = input._1();
            final FileObject resource = parseResult.source;
            final IStrategoTuple inputTerm = input._2();
            try {
                logger.trace("Analysing {}", resource);
                final IStrategoTerm resultTerm = strategoCommon.invoke(interpreter, inputTerm, analysisStrategy);
                if(resultTerm == null) {
                    logger.trace("Analysis for {} failed", resource);
                    results.add(result(analysisCommon.analysisFailedMessage(interpreter), parseResult, context, null));
                } else if(!(resultTerm instanceof IStrategoTuple)) {
                    logger.trace("Analysis for {} has unexpected result, not a tuple", resource);
                    results.add(result(String.format("Unexpected results from analysis %s", resultTerm), parseResult,
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
                    results.add(result(String.format("Unexpected results from analysis %s", resultTerm), parseResult,
                        context, null));
                }
            } catch(MetaborgException e) {
                logger.trace("Analysis for {} failed", resource);
                results.add(result(analysisCommon.analysisFailedMessage(interpreter), parseResult, context, e));
            }
        }

        return new AnalysisResult<>(context, results);
    }

    private ISpoofaxAnalyzeUnit result(IStrategoTerm result,
        ISpoofaxParseUnit parseResult, IContext context) {
        final IStrategoTerm ast = result.getSubterm(0);
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(1)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(2)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(3)));
        messages.addAll(analysisCommon.ambiguityMessages(source, ast));
        return new AnalysisFileResult<>(ast, source, context, messages, parseResult);
    }

    private ISpoofaxAnalyzeUnit resultNoAst(IStrategoTerm result,
        ISpoofaxParseUnit parseResult, IContext context) {
        final FileObject source = parseResult.source;
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(0)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(1)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(2)));
        return new AnalysisFileResult<>(null, source, context, messages, parseResult);
    }

    private ISpoofaxAnalyzeUnit result(String errorString,
        ISpoofaxParseUnit parseResult, IContext context, Throwable e) {
        if(e != null) {
            logger.error(errorString, e);
        } else {
            logger.error(errorString);
        }
        final FileObject source = parseResult.source;
        final IMessage message = MessageFactory.newAnalysisErrorAtTop(source, errorString, e);
        return new AnalysisFileResult<>(null, source, context, Iterables2.singleton(message), parseResult);
    }
}
