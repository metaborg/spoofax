package org.metaborg.spoofax.core.analysis.legacy;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
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
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Analyzer for legacy Stratego projects. Calls the analysis strategy for each input.
 */
public class StrategoAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "stratego";

    private static final ILogger logger = LoggerUtils.logger(StrategoAnalyzer.class);

    private final ISpoofaxUnitService unitService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService runtimeService;

    private final IStrategoCommon strategoCommon;
    private final AnalysisCommon analysisCommon;


    @Inject public StrategoAnalyzer(ISpoofaxUnitService unitService, ITermFactoryService termFactoryService,
            IStrategoRuntimeService runtimeService, IStrategoCommon strategoCommon,
        AnalysisCommon analysisCommon) {
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.analysisCommon = analysisCommon;
    }


    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context, IProgress progress,
        ICancel cancel) throws AnalysisException, InterruptedException {
        cancel.throwIfCancelled();
        
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(context, message);
        }

        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<AnalysisFacet> facetContribution = language.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", language);
            final ISpoofaxAnalyzeUnit emptyUnit = unitService.emptyAnalyzeUnit(input, context);
            return new SpoofaxAnalyzeResult(emptyUnit, context);
        }
        final AnalysisFacet facet = facetContribution.facet;

        cancel.throwIfCancelled();
        final HybridInterpreter runtime;
        try {
            runtime = runtimeService.runtime(facetContribution.contributor, context, true);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }

        cancel.throwIfCancelled();
        final ISpoofaxAnalyzeUnit result = analyze(input, context, runtime, facet.strategyName, termFactory);
        return new SpoofaxAnalyzeResult(result, context);
    }

    @Override public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext context,
        IProgress progress, ICancel cancel) throws AnalysisException, InterruptedException {
        cancel.throwIfCancelled();
        
        final ILanguageImpl language = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();

        final FacetContribution<AnalysisFacet> facetContribution = language.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", language);
            return new SpoofaxAnalyzeResults(context);
        }
        final AnalysisFacet facet = facetContribution.facet;

        cancel.throwIfCancelled();
        final HybridInterpreter runtime;
        try {
            runtime = runtimeService.runtime(facetContribution.contributor, context, true);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }

        final int size = Iterables.size(inputs);
        progress.setWorkRemaining(size);
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayListWithCapacity(size);
        for(ISpoofaxParseUnit input : inputs) {
            cancel.throwIfCancelled();
            if(!input.valid()) {
                logger.warn("Parse input for {} is invalid, cannot analyze", input.source());
                // TODO: throw exception instead?
                progress.work(1);
                continue;
            }
            final ISpoofaxAnalyzeUnit result = analyze(input, context, runtime, facet.strategyName, termFactory);
            results.add(result);
            progress.work(1);
        }
        return new SpoofaxAnalyzeResults(results, context);
    }

    private ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit input, IContext context, HybridInterpreter runtime,
        String strategy, ITermFactory termFactory) throws AnalysisException {
        final FileObject source = input.source();

        final IStrategoString contextPath = strategoCommon.locationTerm(context.location());
        final IStrategoString resourcePath = strategoCommon.resourceTerm(source, context.location());
        final IStrategoTuple inputTerm = termFactory.makeTuple(input.ast(), resourcePath, contextPath);

        try {
            logger.trace("Analysing {}", source);
            final Timer timer = new Timer(true);
            final IStrategoTerm resultTerm = strategoCommon.invoke(runtime, inputTerm, strategy);
            final long duration = timer.stop();
            if(resultTerm == null) {
                logger.trace("Analysis for {} failed", source);
                return result(analysisCommon.analysisFailedMessage(runtime), input, context, null, duration);
            } else if(!(resultTerm instanceof IStrategoTuple)) {
                logger.trace("Analysis for {} has unexpected result, not a tuple", source);
                final String message = logger.format("Unexpected results from analysis {}", resultTerm);
                return result(message, input, context, null, duration);
            } else if(resultTerm.getSubtermCount() == 4) {
                logger.trace("Analysis for {} done", source);
                return result(resultTerm, input, context, duration);
            } else if(resultTerm.getSubtermCount() == 3) {
                logger.trace("Analysis for {} done", source);
                return resultNoAst(resultTerm, input, context, duration);
            } else {
                logger.trace("Analysis for {} has unexpected result; tuple with more than 4 or less than 2 elements",
                    source);
                final String message = logger.format("Unexpected results from analysis {}", resultTerm);
                return result(message, input, context, null, duration);
            }
        } catch(MetaborgException e) {
            final String message = logger.format("Analysis for {} failed", source);
            logger.trace(message, e);
            throw new AnalysisException(context, message, e);
        }
    }


    private ISpoofaxAnalyzeUnit result(IStrategoTerm result, ISpoofaxParseUnit input, IContext context, long duration) {
        final IStrategoTerm ast = result.getSubterm(0);
        final FileObject source = input.source();

        final Collection<IMessage> errors =
            analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(1));
        final Collection<IMessage> warnings =
            analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(2));
        final Collection<IMessage> notes = analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(3));
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

    private ISpoofaxAnalyzeUnit resultNoAst(IStrategoTerm result, ISpoofaxParseUnit input, IContext context,
        long duration) {
        final FileObject source = input.source();

        final Collection<IMessage> errors =
            analysisCommon.messages(source, MessageSeverity.ERROR, result.getSubterm(0));
        final Collection<IMessage> warnings =
            analysisCommon.messages(source, MessageSeverity.WARNING, result.getSubterm(1));
        final Collection<IMessage> notes = analysisCommon.messages(source, MessageSeverity.NOTE, result.getSubterm(2));

        final Collection<IMessage> messages =
            Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size());
        messages.addAll(errors);
        messages.addAll(warnings);
        messages.addAll(notes);

        return unitService.analyzeUnit(input,
            new AnalyzeContrib(true, errors.isEmpty(), false, null, messages, duration), context);
    }

    private ISpoofaxAnalyzeUnit result(String error, ISpoofaxParseUnit input, IContext context, Throwable e,
        long duration) {
        final FileObject source = input.source();
        final IMessage message = MessageFactory.newAnalysisErrorAtTop(source, error, e);
        return unitService.analyzeUnit(input,
            new AnalyzeContrib(false, false, true, null, Iterables2.singleton(message), duration), context);
    }
}
