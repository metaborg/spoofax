package org.metaborg.spoofax.core.analysis.taskengine;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoReal;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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


    @Inject public TaskEngineAnalyzer(IResourceService resourceService, ISpoofaxUnitService unitService,
        ITermFactoryService termFactoryService, IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon,
        AnalysisCommon analysisCommon) {
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.analysisCommon = analysisCommon;

        this.fileCons = termFactoryService.getGeneric().makeConstructor("File", 3);
    }


    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context, IProgress progress,
        ICancel cancel) throws AnalysisException, InterruptedException {
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(context, message);
        }

        final ISpoofaxAnalyzeResults results = analyzeAll(Iterables2.singleton(input), context, progress, cancel);
        return new SpoofaxAnalyzeResult(results.results().iterator().next(), results.updates(), context);
    }


    @Override public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context,
        IProgress progress, ICancel cancel) throws AnalysisException, InterruptedException {
        cancel.throwIfCancelled();

        final ILanguageImpl langImpl = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<AnalysisFacet> facetContribution = langImpl.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", langImpl);
            return new SpoofaxAnalyzeResults(context);
        }
        final AnalysisFacet facet = facetContribution.facet;

        cancel.throwIfCancelled();
        final HybridInterpreter runtime;
        try {
            runtime = runtimeService.runtime(facetContribution.contributor, context, false);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }

        cancel.throwIfCancelled();
        return analyzeAll(inputs, context, runtime, facet.strategyName, termFactory);
    }


    private ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context,
        HybridInterpreter runtime, String strategy, ITermFactory termFactory) throws AnalysisException {
        final Map<String, ISpoofaxParseUnit> inputsPerSource = Maps.newHashMap();
        int detachedCounter = 0;
        final Collection<IStrategoAppl> analysisInputs = Lists.newArrayList();
        for(ISpoofaxParseUnit input : inputs) {
            if(!input.valid()) {
                logger.warn("Parse result for {} is invalid, cannot analyze", input.source());
                continue;
            }
            final String pathString;
            if(input.detached()) {
                pathString = "detached-source-" + detachedCounter++;
                logger.debug("Parse input is detached, using '{}' as path", pathString);
            } else {
                pathString = input.source().getName().getURI();
            }
            inputsPerSource.put(pathString, input);
            final IStrategoString pathTerm = termFactory.makeString(pathString);
            final IStrategoReal durationTerm = termFactory.makeReal(input.duration());
            analysisInputs.add(termFactory.makeAppl(fileCons, pathTerm, input.ast(), durationTerm));
        }
        final IStrategoTerm inputTerm = termFactory.makeList(analysisInputs);

        logger.trace("Invoking {} strategy", strategy);
        final IStrategoTerm resultTerm;
        try {
            resultTerm = strategoCommon.invoke(runtime, inputTerm, strategy);
        } catch(MetaborgException e) {
            final String message = analysisCommon.analysisFailedMessage(runtime);
            throw new AnalysisException(context, message, e);
        }
        if(resultTerm == null) {
            final String message = analysisCommon.analysisFailedMessage(runtime);
            throw new AnalysisException(context, message);
        }
        if(!(resultTerm instanceof IStrategoAppl) || resultTerm.getSubtermCount() != 5) {
            final String message = logger.format("Unexpected results from analysis {}, expected 5-tuple", resultTerm);
            throw new AnalysisException(context, message);
        }

        final IStrategoTerm resultsTerm = resultTerm.getSubterm(0);
        final IStrategoTerm updateResultsTerm = resultTerm.getSubterm(1);

        final Collection<ISpoofaxAnalyzeUnit> fileResults =
            Lists.newArrayListWithCapacity(resultsTerm.getSubtermCount());
        for(IStrategoTerm result : resultsTerm) {
            // HACK: analysis duration per parse unit is unknown, pass -1 as duration.
            final ISpoofaxAnalyzeUnit fileResult = result(result, inputsPerSource, context, -1);
            if(fileResult == null) {
                continue;
            }
            fileResults.add(fileResult);
        }

        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults =
            Lists.newArrayListWithCapacity(updateResultsTerm.getSubtermCount());
        for(IStrategoTerm result : updateResultsTerm) {
            final ISpoofaxAnalyzeUnitUpdate updateResult = updateResult(result, context);
            if(updateResult == null) {
                continue;
            }
            updateResults.add(updateResult);
        }

        // Currently unused debugging and performance data.
        // final IStrategoTerm affectedPartitionsTerm = resultTerm.getSubterm(2);
        // final IStrategoTerm debugResultTerm = resultTerm.getSubterm(3);
        // final IStrategoTerm timeResultTerm = resultTerm.getSubterm(4);
        // final Collection<String> affectedPartitions = affectedPartitions(affectedPartitionsTerm);
        // final AnalysisDebugResult debugResult = debugResult(debugResultTerm);
        // final AnalysisTimeResult timeResult = timeResult(timeResultTerm);
        // final TaskEngineAnalyzerData data = new TaskEngineAnalyzerData(affectedPartitions, debugResult, timeResult);

        return new SpoofaxAnalyzeResults(fileResults, updateResults, context);
    }

    private @Nullable ISpoofaxAnalyzeUnit result(IStrategoTerm result, Map<String, ISpoofaxParseUnit> inputsPerSource,
        IContext context, long duration) {
        final String sourceString = Tools.asJavaString(result.getSubterm(0));
        final FileObject source;
        try {
            source = resourceService.resolve(sourceString);
        } catch(MetaborgRuntimeException e) {
            logger.error("Cannot find original source for {}, skipping result", e, sourceString);
            return null;
        }

        final ISpoofaxParseUnit input = inputsPerSource.get(sourceString);
        if(input == null) {
            logger.error("Cannot find input parse result for {}, skipping result", sourceString);
            return null;
        }

        final IStrategoTerm ast = result.getSubterm(2);

        final Collection<IMessage> errors =
            analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(3));
        final Collection<IMessage> warnings =
            analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(4));
        final Collection<IMessage> notes = analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(5));
        final Collection<IMessage> ambiguities = analysisCommon.ambiguityMessages(source, ast);

        final Collection<IMessage> messages =
            Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
        messages.addAll(errors);
        messages.addAll(warnings);
        messages.addAll(notes);
        messages.addAll(ambiguities);

        return unitService.analyzeUnit(input, new AnalyzeContrib(true, errors.isEmpty(), true, ast, messages, duration),
            context);
    }

    private ISpoofaxAnalyzeUnitUpdate updateResult(IStrategoTerm result, IContext context) {
        final String sourceString = Tools.asJavaString(result.getSubterm(0));
        final FileObject source;
        try {
            source = resourceService.resolve(sourceString);
        } catch(MetaborgRuntimeException e) {
            logger.error("Cannot find original source for {}, skipping update result", e, sourceString);
            return null;
        }

        final Collection<IMessage> errors =
            analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(1));
        final Collection<IMessage> warnings =
            analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(2));
        final Collection<IMessage> notes = analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(3));

        final Collection<IMessage> messages =
            Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size());
        messages.addAll(errors);
        messages.addAll(warnings);
        messages.addAll(notes);

        return unitService.analyzeUnitUpdate(source, new AnalyzeUpdateData(messages), context);
    }

    /* Currently unused debugging and performance data. @formatter:off
    private Collection<String> affectedPartitions(IStrategoTerm affectedTerm) {
        final Collection<String> affected = new ArrayList<>(affectedTerm.getSubtermCount());
        for(IStrategoTerm partition : affectedTerm) {
            affected.add(Tools.asJavaString(partition));
        }
        return affected;
    }

    private AnalysisDebugResult debugResult(IStrategoTerm debug) {
        final IStrategoTerm collectionDebug = debug.getSubterm(0);
        return new AnalysisDebugResult(Tools.asJavaInt(collectionDebug.getSubterm(0)),
            Tools.asJavaInt(collectionDebug.getSubterm(1)), Tools.asJavaInt(collectionDebug.getSubterm(2)),
            Tools.asJavaInt(collectionDebug.getSubterm(3)), Tools.asJavaInt(collectionDebug.getSubterm(4)),
            (IStrategoList) debug.getSubterm(1), (IStrategoList) debug.getSubterm(2),
            (IStrategoList) debug.getSubterm(3));
    }

    private AnalysisTimeResult timeResult(IStrategoTerm time) {
        return new AnalysisTimeResult((long) Tools.asJavaDouble(time.getSubterm(0)),
            (long) Tools.asJavaDouble(time.getSubterm(1)), (long) Tools.asJavaDouble(time.getSubterm(2)),
            (long) Tools.asJavaDouble(time.getSubterm(3)), (long) Tools.asJavaDouble(time.getSubterm(4)),
            (long) Tools.asJavaDouble(time.getSubterm(5)), (long) Tools.asJavaDouble(time.getSubterm(6)));
    }
    @formatter:on */
}
