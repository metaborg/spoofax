package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.ImmutableMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.MessageContent;
import org.metaborg.meta.nabl2.constraints.messages.MessageKind;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.SolverException;
import org.metaborg.meta.nabl2.spoofax.analysis.Actions;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.ITermVar;
import org.metaborg.meta.nabl2.terms.generic.GenericTerms;
import org.metaborg.meta.nabl2.util.Optionals;
import org.metaborg.meta.nabl2.util.functions.Function1;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.IMultiFileScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IMultiFileScopeGraphUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.metaborg.util.time.AggregateTimer;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer<IMultiFileScopeGraphContext>
    implements ISpoofaxAnalyzer {

    public static final String name = "constraint-multifile";

    private static final ILogger logger = LoggerUtils.logger(ConstraintMultiFileAnalyzer.class);

    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
        final ISpoofaxUnitService unitService, final IResourceService resourceService,
        final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
        final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.unitService = unitService;
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed,
        Map<String, ISpoofaxParseUnit> removed, IMultiFileScopeGraphContext context, HybridInterpreter runtime,
        String strategy, IProgress progress, ICancel cancel) throws AnalysisException {
        final Timer totalTimer = new Timer(true);
        final AggregateTimer collectionTimer = new AggregateTimer();
        final AggregateTimer solverTimer = new AggregateTimer();
        final AggregateTimer finalizeTimer = new AggregateTimer();
        final String globalSource = context.location().getName().getURI();

        for(String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final int n = changed.size();
        final int w = context.units().size() / 2;
        progress.setWorkRemaining(n + w + 1);

        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults = Lists.newArrayList();
        try {

            // initial
            InitialResult initialResult;
            final Optional<ITerm> customInitial;
            {
                if(context.initialResult().isPresent()) {
                    initialResult = context.initialResult().get();
                    customInitial = context.initialResult().flatMap(r -> r.getCustomResult());
                } else {
                    collectionTimer.start();
                    try {
                        ITerm initialResultTerm =
                            doAction(strategy, Actions.analyzeInitial(globalSource), context, runtime)
                                .orElseThrow(() -> new AnalysisException(context, "No initial result."));
                        initialResult = InitialResult.matcher().match(initialResultTerm)
                            .orElseThrow(() -> new AnalysisException(context, "Invalid initial results."));
                        customInitial = doCustomAction(strategy, Actions.customInitial(globalSource), context, runtime);
                        initialResult = initialResult.setCustomResult(customInitial);
                        context.setInitialResult(initialResult);
                    } finally {
                        collectionTimer.stop();
                    }
                }
            }

            // global parameters, that form the interface for incremental solver
            final List<ITerm> globalTerms = Lists.newArrayList();
            {
                Iterables.addAll(globalTerms, initialResult.getArgs().getParams());
                initialResult.getArgs().getType().ifPresent(type -> globalTerms.add(type));
            }

            // units
            final Map<String, IStrategoTerm> astsByFile = Maps.newHashMap();
            final Map<String, IMessage> failures = Maps.newHashMap();
            final Multimap<String, IMessage> ambiguitiesByFile = HashMultimap.create();
            for(Map.Entry<String, ISpoofaxParseUnit> input : changed.entrySet()) {
                final String source = input.getKey();
                final ISpoofaxParseUnit parseUnit = input.getValue();
                final ITerm ast = strategoTerms.fromStratego(parseUnit.ast());

                final IMultiFileScopeGraphUnit unit = context.unit(source);
                unit.clear();

                try {
                    UnitResult unitResult;
                    final Optional<ITerm> customUnit;
                    {
                        collectionTimer.start();
                        try {
                            final ITerm unitResultTerm =
                                doAction(strategy, Actions.analyzeUnit(source, ast, initialResult.getArgs()), context,
                                    runtime).orElseThrow(() -> new AnalysisException(context, "No unit result."));
                            unitResult = UnitResult.matcher().match(unitResultTerm)
                                .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                            final ITerm desugaredAST = unitResult.getAST();
                            customUnit = doCustomAction(strategy, Actions.customUnit(source, desugaredAST,
                                customInitial.orElse(GenericTerms.EMPTY_TUPLE)), context, runtime);
                            unitResult = unitResult.setCustomResult(customUnit);
                            final IStrategoTerm analyzedAST = strategoTerms.toStratego(desugaredAST);
                            astsByFile.put(source, analyzedAST);
                            ambiguitiesByFile.putAll(source,
                                analysisCommon.ambiguityMessages(parseUnit.source(), analyzedAST));
                            unit.setUnitResult(unitResult);
                        } finally {
                            collectionTimer.stop();
                        }
                    }

                    {
                        final Iterable<IConstraint> unitConstraints;
                        final IProjectConfig config = context.project().config();
                        if(config != null && config.incrementalConstraintSolver()) {
                            try {
                                solverTimer.start();
                                Function1<String, ITermVar> fresh =
                                    base -> GenericTerms.newVar(source, context.unit(source).fresh().fresh(base));
                                IMessageInfo messageInfo = ImmutableMessageInfo.of(MessageKind.ERROR,
                                    MessageContent.of(), Actions.sourceTerm(source));
                                unitConstraints = Solver.solveIncremental(initialResult.getConfig(), globalTerms, fresh,
                                    unitResult.getConstraints(), messageInfo, progress.subProgress(1), cancel);
                            } catch(SolverException e) {
                                throw new AnalysisException(context, e);
                            } finally {
                                solverTimer.stop();
                            }
                        } else {
                            unitConstraints = unitResult.getConstraints();
                        }
                        unit.setNormalizedConstraints(unitConstraints);
                    }

                } catch(MetaborgException e) {
                    logger.warn("File analysis failed.", e);
                    failures.put(source,
                        MessageFactory.newAnalysisErrorAtTop(parseUnit.source(), "File analysis failed.", e));
                }
            }

            // solve
            final Solution solution;
            final List<Optional<ITerm>> customUnits = Lists.newArrayList();
            {
                final List<Iterable<IConstraint>> constraints = Lists.newArrayList();
                context.initialResult().ifPresent(i -> constraints.add(i.getConstraints()));
                for(IMultiFileScopeGraphUnit unit : context.units()) {
                    unit.normalizedConstraints().ifPresent(constraints::add);
                    unit.unitResult().map(UnitResult::getCustomResult).ifPresent(customUnits::add);
                }
                try {
                    solverTimer.start();
                    Function1<String, ITermVar> fresh =
                        base -> GenericTerms.newVar(globalSource, context.unit(globalSource).fresh().fresh(base));
                    IMessageInfo messageInfo = ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(),
                        Actions.sourceTerm(globalSource));
                    solution = Solver.solveFinal(initialResult.getConfig(), fresh, Iterables.concat(constraints),
                        messageInfo, progress.subProgress(w), cancel);
                } catch(SolverException e) {
                    throw new AnalysisException(context, e);
                } finally {
                    solverTimer.stop();
                }
                context.setSolution(solution);
            }

            // final
            FinalResult finalResult;
            final Optional<ITerm> customFinal;
            final Optional<CustomSolution> customSolution;
            {
                finalizeTimer.start();
                try {
                    ITerm finalResultTerm = doAction(strategy, Actions.analyzeFinal(globalSource), context, runtime)
                        .orElseThrow(() -> new AnalysisException(context, "No final result."));
                    finalResult = FinalResult.matcher().match(finalResultTerm)
                        .orElseThrow(() -> new AnalysisException(context, "Invalid final results."));
                    customFinal = doCustomAction(strategy,
                        Actions.customFinal(globalSource, customInitial.orElse(GenericTerms.EMPTY_TUPLE),
                            GenericTerms.newList(Optionals.filter(customUnits))),
                        context, runtime);
                    finalResult = finalResult.setCustomResult(customFinal);
                    context.setFinalResult(finalResult);

                    customSolution = customFinal.flatMap(CustomSolution.matcher()::match);
                    customSolution.ifPresent(cs -> context.setCustomSolution(cs));
                } finally {
                    finalizeTimer.stop();
                }
            }

            // errors
            {
                Multimap<String, IMessage> errorsByFile =
                    messagesByFile(merge(messages(solution, MessageKind.ERROR, MessageSeverity.ERROR), customSolution
                        .map(cs -> messages(cs.getErrors(), MessageSeverity.ERROR)).orElse(Lists.newArrayList())));
                Multimap<String, IMessage> warningsByFile = messagesByFile(
                    merge(messages(solution, MessageKind.WARNING, MessageSeverity.WARNING), customSolution
                        .map(cs -> messages(cs.getErrors(), MessageSeverity.WARNING)).orElse(Lists.newArrayList())));
                Multimap<String, IMessage> notesByFile =
                    messagesByFile(merge(messages(solution, MessageKind.NOTE, MessageSeverity.NOTE), customSolution
                        .map(cs -> messages(cs.getErrors(), MessageSeverity.NOTE)).orElse(Lists.newArrayList())));
                for(IMultiFileScopeGraphUnit unit : context.units()) {
                    final String source = unit.resource();
                    final Collection<IMessage> errors = errorsByFile.get(source);
                    final Collection<IMessage> warnings = warningsByFile.get(source);
                    final Collection<IMessage> notes = notesByFile.get(source);
                    final Collection<IMessage> ambiguities = ambiguitiesByFile.get(source);
                    final Collection<IMessage> messages = Lists
                        .newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
                    messages.addAll(errors);
                    messages.addAll(warnings);
                    messages.addAll(notes);
                    messages.addAll(ambiguities);
                    if(changed.containsKey(source)) {
                        final boolean valid;
                        if(!(valid = !failures.containsKey(source))) {
                            messages.add(failures.get(source));
                        }
                        final boolean success = valid && errors.isEmpty();
                        results.add(unitService.analyzeUnit(changed.get(source),
                            new AnalyzeContrib(valid, success, true, astsByFile.get(source), messages, -1), context));
                    } else {
                        final FileObject file = resourceService.resolve(source);
                        updateResults
                            .add(unitService.analyzeUnitUpdate(file, new AnalyzeUpdateData(messages), context));
                    }
                }
            }

        } catch(InterruptedException e) {
            logger.info("Analysis was interrupted.");
        } finally {
            totalTimer.stop();
        }

        final ConstraintDebugData debugData = new ConstraintDebugData(totalTimer.stop(), collectionTimer.total(),
            solverTimer.total(), finalizeTimer.total());
        logger.info("{}", debugData);
        return new SpoofaxAnalyzeResults(results, updateResults, context, debugData);
    }

}