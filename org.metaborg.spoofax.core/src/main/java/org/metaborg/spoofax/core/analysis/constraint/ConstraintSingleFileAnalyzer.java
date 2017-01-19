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
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.UnsatisfiableException;
import org.metaborg.meta.nabl2.spoofax.analysis.Actions;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableFinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableInitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.ImmutableUnitResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.meta.nabl2.terms.ITerm;
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

public class ConstraintSingleFileAnalyzer extends AbstractConstraintAnalyzer<ISingleFileScopeGraphContext>
        implements ISpoofaxAnalyzer {

    public static final ILogger logger = LoggerUtils.logger(ConstraintSingleFileAnalyzer.class);
    public static final String name = "constraint-singlefile";

    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintSingleFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.unitService = unitService;
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, ISingleFileScopeGraphContext context, HybridInterpreter runtime,
            String strategy) throws AnalysisException {
        for(String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final String globalSource = context.location().getName().getURI();
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        for(Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();
            ITerm ast = strategoTerms.fromStratego(parseUnit.ast());

            try {
                ISingleFileScopeGraphUnit unit = context.unit(source);
                unit.clear();

                // initial
                ITerm initialResultTerm = doAction(strategy, Actions.analyzeInitial(globalSource), context, runtime)
                        .orElseThrow(() -> new AnalysisException(context, "No initial result."));
                InitialResult initialResult = InitialResult.matcher().match(initialResultTerm)
                        .orElseThrow(() -> new MetaborgException("Invalid initial results."));
                Optional<ITerm> customInitial =
                        doCustomAction(strategy, Actions.customInitial(globalSource), context, runtime);
                initialResult = ImmutableInitialResult.copyOf(initialResult).setCustomResult(customInitial);

                // unit
                final ITerm unitResultTerm =
                        doAction(strategy, Actions.analyzeUnit(source, ast, initialResult.getArgs()), context, runtime)
                                .orElseThrow(() -> new AnalysisException(context, "No unit result."));
                UnitResult unitResult = UnitResult.matcher().match(unitResultTerm)
                        .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                final ITerm desugaredAST = unitResult.getAST();
                Optional<ITerm> customUnit = initialResult.getCustomResult().flatMap(initial -> {
                    return doCustomAction(strategy, Actions.customUnit(globalSource, desugaredAST, initial), context, runtime);
                });
                unitResult = ImmutableUnitResult.copyOf(unitResult).setCustomResult(customUnit);
                unit.setUnitResult(unitResult);
                final IStrategoTerm analyzedAST = strategoTerms.toStratego(desugaredAST);

                // solve
                Iterable<IConstraint> constraints =
                        Iterables.concat(initialResult.getConstraints(), unitResult.getConstraints());
                Solution solution = Solver.solve(initialResult.getConfig(), constraints);
                unit.setSolution(solution);

                // final
                ITerm finalResultTerm = doAction(strategy, Actions.analyzeFinal(source), context, runtime)
                        .orElseThrow(() -> new AnalysisException(context, "No final result."));
                FinalResult finalResult = FinalResult.matcher().match(finalResultTerm)
                        .orElseThrow(() -> new MetaborgException("Invalid final results."));
                Optional<ITerm> customFinal = Optionals.lift(initialResult.getCustomResult(), customUnit, (i, u) -> {
                    List<ITerm> us = Lists.newArrayList();
                    us.add(u);
                    return doCustomAction(strategy, Actions.customFinal(source, i, us), context, runtime);
                }).flatMap(o -> o);
                finalResult = ImmutableFinalResult.of().setCustomResult(customFinal);
                unit.setFinalResult(finalResult);

                Optional<CustomSolution> customSolution = customFinal.flatMap(CustomSolution.matcher()::match);
                customSolution.ifPresent(cs -> unit.setCustomSolution(cs));

                // errors
                final Collection<IMessage> errors = messages(parseUnit.source(),
                        merge(solution.getErrors(),
                                customSolution.map(cs -> cs.getErrors()).orElse(Lists.newArrayList())),
                        MessageSeverity.ERROR);
                final Collection<IMessage> warnings = messages(parseUnit.source(),
                        merge(solution.getWarnings(),
                                customSolution.map(cs -> cs.getWarnings()).orElse(Lists.newArrayList())),
                        MessageSeverity.WARNING);
                final Collection<IMessage> notes = messages(parseUnit.source(),
                        merge(solution.getNotes(),
                                customSolution.map(cs -> cs.getNotes()).orElse(Lists.newArrayList())),
                        MessageSeverity.NOTE);
                final Collection<IMessage> ambiguities =
                        analysisCommon.ambiguityMessages(parseUnit.source(), analyzedAST);
                final Collection<IMessage> messages = Lists
                        .newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
                messages.addAll(errors);
                messages.addAll(warnings);
                messages.addAll(notes);
                messages.addAll(ambiguities);

                // result
                results.add(unitService.analyzeUnit(parseUnit,
                        new AnalyzeContrib(true, errors.isEmpty(), true, analyzedAST, messages, -1), context));
            } catch(MetaborgException | UnsatisfiableException e) {
                logger.warn("File analysis failed.", e);
                Iterable<IMessage> messages = Iterables2.singleton(
                        MessageFactory.newAnalysisErrorAtTop(parseUnit.source(), "File analysis failed.", e));
                results.add(unitService.analyzeUnit(parseUnit,
                        new AnalyzeContrib(true, false, false, null, messages, -1), context));
            }
        }
        return new SpoofaxAnalyzeResults(results, Collections.<ISpoofaxAnalyzeUnitUpdate>emptyList(), context);
    }

}