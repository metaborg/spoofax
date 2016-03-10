package org.metaborg.spoofax.core.analysis.taskengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
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
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Analyzer for NaBL + TS + index + task engine projects. Calls the analysis strategy with a list of all inputs.
 */
public class TaskEngineAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "taskengine";

    private static final ILogger logger = LoggerUtils.logger(TaskEngineAnalyzer.class);

    private final IResourceService resourceService;
    private final ISpoofaxUnitService unitService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final IStrategoCommon strategoCommon;
    private final AnalysisCommon analysisCommon;
    private final IStrategoConstructor fileCons;


    @Inject public TaskEngineAnalyzer(IResourceService resourceService, ISpoofaxUnitService unitService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon, AnalysisCommon analysisCommon) {
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.analysisCommon = analysisCommon;

        this.fileCons = termFactoryService.getGeneric().makeConstructor("File", 3);
    }


    @Override public AnalysisResult<IStrategoTerm, IStrategoTerm> analyze(Iterable<ISpoofaxParseUnit> inputs,
        IContext context) throws AnalysisException {
        final ILanguageImpl langImpl = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<AnalysisFacet> facetContribution = langImpl.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", langImpl);
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
        logger.trace("Creating input terms for analysis");
        final Collection<IStrategoAppl> analysisInputs = Lists.newLinkedList();
        for(ISpoofaxParseUnit input : inputs) {
            if(input.result == null) {
                logger.warn("Parse result for {} is null, cannot analyze", input.source);
                continue;
            }
            final String pathString;
            if(input.source == null) {
                logger.debug("Parse result has no source, using 'null' as path");
                pathString = "null";
            } else {
                pathString = input.source.getName().getURI();
            }
            final IStrategoString pathTerm = termFactory.makeString(pathString);
            analysisInputs.add(termFactory.makeAppl(fileCons, pathTerm, input.result,
                termFactory.makeReal(input.duration)));
        }
        final IStrategoTerm inputTerm = termFactory.makeList(analysisInputs);

        logger.trace("Invoking {} strategy", analysisStrategy);
        final IStrategoTerm resultTerm;
        try {
            resultTerm = strategoCommon.invoke(interpreter, inputTerm, analysisStrategy);
        } catch(MetaborgException e) {
            final String message = analysisCommon.analysisFailedMessage(interpreter);
            logger.error(message, e);
            throw new AnalysisException(context, message, e);
        }
        if(resultTerm == null) {
            final String message = analysisCommon.analysisFailedMessage(interpreter);
            logger.error(message);
            throw new AnalysisException(context, message);
        }
        if(!(resultTerm instanceof IStrategoAppl)) {
            final String message = String.format("Unexpected results from analysis %s", resultTerm);
            logger.error(message);
            throw new AnalysisException(context, message);
        }

        logger.trace("Analysis resulted in a {} term", resultTerm.getSubtermCount());
        final IStrategoTerm fileResultsTerm = resultTerm.getSubterm(0);
        final IStrategoTerm messageResultsTerm = resultTerm.getSubterm(1);
        final IStrategoTerm affectedPartitionsTerm = resultTerm.getSubterm(2);
        final IStrategoTerm debugResultTerm = resultTerm.getSubterm(3);
        final IStrategoTerm timeResultTerm = resultTerm.getSubterm(4);

        final int numItems = fileResultsTerm.getSubtermCount();
        logger.trace("Analysis contains {} results. Converting to analysis results", numItems);
        final Collection<ISpoofaxAnalyzeUnit> fileResults = Lists.newLinkedList();
        for(IStrategoTerm result : fileResultsTerm) {
            final ISpoofaxAnalyzeUnit fileResult = fileResult(result, context);
            if(fileResult == null) {
                continue;
            }
            fileResults.add(fileResult);
        }
        final Collection<AnalysisMessageResult> messageResults = Lists.newLinkedList();
        for(IStrategoTerm result : messageResultsTerm) {
            final AnalysisMessageResult messageResult = messageResult(result);
            messageResults.add(messageResult);
        }

        final Collection<String> affectedPartitions = affectedPartitions(affectedPartitionsTerm);
        final AnalysisDebugResult debugResult = debugResult(debugResultTerm);
        final AnalysisTimeResult timeResult = timeResult(timeResultTerm);

        final IAnalyzerData data = new TaskEngineAnalyzerData(affectedPartitions, debugResult, timeResult);
        return new AnalysisResult<>(context, fileResults, messageResults, data);
    }

    private @Nullable ISpoofaxAnalyzeUnit
        fileResult(IStrategoTerm result, IContext context) {
        final String file = Tools.asJavaString(result.getSubterm(0));
        final FileObject source = resourceService.resolve(file);
        if(source == null) {
            logger.error("Cannot find original source for {}, skipping result", file);
            return null;
        }
        final IStrategoTerm previousAst = result.getSubterm(1);
        final IStrategoTerm ast = result.getSubterm(2);
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(3)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(4)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(5)));
        messages.addAll(analysisCommon.ambiguityMessages(source, ast));

        return new AnalysisFileResult<>(ast, source, context, messages, new ParseResult<>("", previousAst, source,
            Arrays.asList(new IMessage[] {}), -1, context.language(), null, null));
    }

    private AnalysisMessageResult messageResult(IStrategoTerm result) {
        final String file = Tools.asJavaString(result.getSubterm(0));
        final FileObject source = resourceService.resolve(file);
        final Collection<IMessage> messages = Lists.newLinkedList();
        messages.addAll(analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(1)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(2)));
        messages.addAll(analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(3)));
        return new AnalysisMessageResult(source, messages);
    }

    private Collection<String> affectedPartitions(IStrategoTerm affectedTerm) {
        final Collection<String> affected = new ArrayList<>(affectedTerm.getSubtermCount());
        for(IStrategoTerm partition : affectedTerm) {
            affected.add(Tools.asJavaString(partition));
        }
        return affected;
    }

    private AnalysisDebugResult debugResult(IStrategoTerm debug) {
        final IStrategoTerm collectionDebug = debug.getSubterm(0);
        return new AnalysisDebugResult(Tools.asJavaInt(collectionDebug.getSubterm(0)), Tools.asJavaInt(collectionDebug
            .getSubterm(1)), Tools.asJavaInt(collectionDebug.getSubterm(2)), Tools.asJavaInt(collectionDebug
            .getSubterm(3)), Tools.asJavaInt(collectionDebug.getSubterm(4)), (IStrategoList) debug.getSubterm(1),
            (IStrategoList) debug.getSubterm(2), (IStrategoList) debug.getSubterm(3));
    }

    private AnalysisTimeResult timeResult(IStrategoTerm time) {
        return new AnalysisTimeResult((long) Tools.asJavaDouble(time.getSubterm(0)), (long) Tools.asJavaDouble(time
            .getSubterm(1)), (long) Tools.asJavaDouble(time.getSubterm(2)), (long) Tools.asJavaDouble(time
            .getSubterm(3)), (long) Tools.asJavaDouble(time.getSubterm(4)), (long) Tools.asJavaDouble(time
            .getSubterm(5)), (long) Tools.asJavaDouble(time.getSubterm(6)));
    }
}
