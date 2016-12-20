package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.ImmutableSolution;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.UnsatisfiableException;
import org.metaborg.meta.nabl2.spoofax.Actions;
import org.metaborg.meta.nabl2.spoofax.FinalResult;
import org.metaborg.meta.nabl2.spoofax.ImmutableFinalResult;
import org.metaborg.meta.nabl2.spoofax.ImmutableInitialResult;
import org.metaborg.meta.nabl2.spoofax.ImmutableUnitResult;
import org.metaborg.meta.nabl2.spoofax.InitialResult;
import org.metaborg.meta.nabl2.spoofax.ResultTerms;
import org.metaborg.meta.nabl2.spoofax.UnitResult;
import org.metaborg.meta.nabl2.util.Optionals;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.ISingleFileScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ISingleFileScopeGraphUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConstraintSingleFileAnalyzer extends AbstractConstraintAnalyzer<ISingleFileScopeGraphContext> implements
        ISpoofaxAnalyzer {

    public static final ILogger logger = LoggerUtils.logger(ConstraintSingleFileAnalyzer.class);
    public static final String name = "constraint-singlefile";

    private final ISpoofaxUnitService unitService;
    private final Actions actionBuilder;

    @Inject public ConstraintSingleFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.unitService = unitService;
        this.actionBuilder = new Actions(termFactory, strategoTerms);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, ISingleFileScopeGraphContext context, HybridInterpreter runtime,
            String strategy) throws AnalysisException {
        for (String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final String globalSource = context.location().getName().getURI();
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        for (Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();

            try {
                ISingleFileScopeGraphUnit unit = context.unit(source);
                unit.clear();

                // initial
                IStrategoTerm initialResultTerm = doAction(strategy, actionBuilder.analyzeInitial(globalSource),
                        context, runtime).orElseThrow(() -> new AnalysisException(context, "No initial result."));
                InitialResult initialResult = ResultTerms.initialOf().match(strategoTerms.fromStratego(
                        initialResultTerm)).orElseThrow(() -> new MetaborgException("Invalid initial results."));
                Optional<IStrategoTerm> customInitial = doCustomAction(strategy, actionBuilder.customInitial(
                        globalSource), context, runtime);
                initialResult = ImmutableInitialResult.copyOf(initialResult).setCustomResult(customInitial);

                // unit
                IStrategoTerm unitResultTerm = doAction(strategy, actionBuilder.analyzeUnit(source, parseUnit.ast(),
                        initialResult.getArgs()), context, runtime).orElseThrow(() -> new AnalysisException(context,
                                "No unit result."));
                UnitResult unitResult = ResultTerms.unitOf().match(strategoTerms.fromStratego(unitResultTerm))
                        .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                Optional<IStrategoTerm> customUnit = initialResult.getCustomResult().flatMap(initial -> {
                    return doCustomAction(strategy, actionBuilder.customUnit(globalSource, parseUnit.ast(), initial),
                            context, runtime);
                });
                unitResult = ImmutableUnitResult.copyOf(unitResult).setCustomResult(customUnit);
                unit.setUnitResult(unitResult);

                // solve
                Iterable<IConstraint> constraints = Iterables.concat(initialResult.getConstraints(), unitResult
                        .getConstraints());
                Solution solution = Solver.solve(initialResult.getConfig(), constraints);

                // final
                IStrategoTerm finalResultTerm = doAction(strategy, actionBuilder.analyzeFinal(source), context, runtime)
                        .orElseThrow(() -> new AnalysisException(context, "No final result."));
                FinalResult finalResult = ResultTerms.finalOf().match(strategoTerms.fromStratego(finalResultTerm))
                        .orElseThrow(() -> new MetaborgException("Invalid final results."));
                Optional<IStrategoTerm> customFinal = Optionals.lift(initialResult.getCustomResult(), customUnit, (i,
                        u) -> {
                    List<IStrategoTerm> us = Lists.newArrayList();
                    us.add(u);
                    return doCustomAction(strategy, actionBuilder.customFinal(globalSource, i, us), context, runtime);
                }).flatMap(o -> o);
                finalResult = ImmutableFinalResult.of().setCustomResult(customFinal);
                unit.setFinalResult(finalResult);

                solution = ImmutableSolution.copyOf(solution).setCustom(customFinal);
                unit.setSolution(solution);
                IStrategoTerm analyzedAST = strategoTerms.toStratego(unitResult.getAST());

                // errors
                final Collection<IMessage> errors = messages(parseUnit.source(), solution.getErrors(),
                        MessageSeverity.ERROR);
                final Collection<IMessage> warnings = messages(parseUnit.source(), solution.getWarnings(),
                        MessageSeverity.WARNING);
                final Collection<IMessage> notes = messages(parseUnit.source(), solution.getNotes(),
                        MessageSeverity.NOTE);
                final Collection<IMessage> ambiguities = analysisCommon.ambiguityMessages(parseUnit.source(),
                        analyzedAST);
                final Collection<IMessage> messages = Lists.newArrayListWithCapacity(errors.size() + warnings.size()
                        + notes.size() + ambiguities.size());
                messages.addAll(errors);
                messages.addAll(warnings);
                messages.addAll(notes);
                messages.addAll(ambiguities);

                // result
                results.add(unitService.analyzeUnit(parseUnit, new AnalyzeContrib(true, errors.isEmpty(), true,
                        analyzedAST, messages, -1), context));
            } catch (MetaborgException | UnsatisfiableException e) {
                logger.warn("File analysis failed.", e);
                Iterable<IMessage> messages = Iterables2.singleton(MessageFactory.newAnalysisErrorAtTop(parseUnit
                        .source(), "File analysis failed.", e));
                results.add(unitService.analyzeUnit(parseUnit, new AnalyzeContrib(true, false, false, null, messages,
                        -1), context));
            }
        }
        return new SpoofaxAnalyzeResults(results, Collections.<ISpoofaxAnalyzeUnitUpdate> emptyList(), context);
    }

}