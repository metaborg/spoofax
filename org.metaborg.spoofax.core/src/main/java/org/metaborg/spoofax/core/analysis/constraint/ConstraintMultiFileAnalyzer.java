package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.nabl2.config.NaBL2DebugConfig;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.ImmutableMessageInfo;
import org.metaborg.meta.nabl2.constraints.messages.MessageContent;
import org.metaborg.meta.nabl2.constraints.messages.MessageKind;
import org.metaborg.meta.nabl2.solver.ImmutablePartialSolution;
import org.metaborg.meta.nabl2.solver.PartialSolution;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.SolverException;
import org.metaborg.meta.nabl2.solver.messages.EmptyMessages;
import org.metaborg.meta.nabl2.solver.messages.Messages;
import org.metaborg.meta.nabl2.spoofax.analysis.Actions;
import org.metaborg.meta.nabl2.spoofax.analysis.CustomSolution;
import org.metaborg.meta.nabl2.spoofax.analysis.FinalResult;
import org.metaborg.meta.nabl2.spoofax.analysis.InitialResult;
import org.metaborg.meta.nabl2.spoofax.analysis.UnitResult;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.ITermVar;
import org.metaborg.meta.nabl2.terms.generic.TB;
import org.metaborg.meta.nabl2.unification.EmptyUnifier;
import org.metaborg.meta.nabl2.util.Optionals;
import org.metaborg.meta.nabl2.util.collections.IRelation3;
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
import org.metaborg.util.stream.Collectors2;
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

    private static final ILogger logger = LoggerUtils.logger(ConstraintMultiFileAnalyzer.class);

    public static final String name = "constraint-multifile";

    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, resourceService, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.unitService = unitService;
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed, Set<String> removed,
            IMultiFileScopeGraphContext context, HybridInterpreter runtime, String strategy, IProgress progress,
            ICancel cancel) throws AnalysisException {
        final NaBL2DebugConfig debugConfig = context.config().debug();
        final Timer totalTimer = new Timer(true);
        final AggregateTimer collectionTimer = new AggregateTimer();
        final AggregateTimer solverTimer = new AggregateTimer();
        final AggregateTimer finalizeTimer = new AggregateTimer();

        final String globalSource = "";

        for(String input : removed) {
            context.removeUnit(input);
        }

        final int n = changed.size();
        final int w = context.units().size() / 2;
        progress.setWorkRemaining(n + w + 1);

        final boolean incremental = context.config().incremental();

        if(debugConfig.analysis() || debugConfig.files()) {
            logger.info("Analyzing {} files in {}.", n, context.location());
        }
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults = Lists.newArrayList();
        try {

            // initial
            InitialResult initialResult;
            final Optional<ITerm> customInitial;
            {
                if(debugConfig.collection()) {
                    logger.info("Collecting initial constraints.");
                }
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
                        initialResult = initialResult.withCustomResult(customInitial);
                        context.setInitialResult(initialResult);
                    } finally {
                        collectionTimer.stop();
                    }
                }
                if(debugConfig.collection()) {
                    logger.info("Initial constraints collected.");
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

                if(debugConfig.files()) {
                    logger.info("Analyzing {}.", source);
                }
                final IMultiFileScopeGraphUnit unit = context.unit(source);
                unit.clear();

                try {
                    UnitResult unitResult;
                    final Optional<ITerm> customUnit;
                    {
                        if(debugConfig.collection()) {
                            logger.info("Collecting constraints of {}.", source);
                        }
                        try {
                            collectionTimer.start();
                            final ITerm unitResultTerm = doAction(strategy,
                                    Actions.analyzeUnit(source, ast, initialResult.getArgs()), context, runtime)
                                            .orElseThrow(() -> new AnalysisException(context, "No unit result."));
                            unitResult = UnitResult.matcher().match(unitResultTerm)
                                    .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                            final ITerm desugaredAST = unitResult.getAST();
                            customUnit = doCustomAction(strategy,
                                    Actions.customUnit(source, desugaredAST, customInitial.orElse(TB.EMPTY_TUPLE)),
                                    context, runtime);
                            unitResult = unitResult.withCustomResult(customUnit);
                            final IStrategoTerm analyzedAST = strategoTerms.toStratego(desugaredAST);
                            astsByFile.put(source, analyzedAST);
                            ambiguitiesByFile.putAll(source,
                                    analysisCommon.ambiguityMessages(parseUnit.source(), analyzedAST));
                            unit.setUnitResult(unitResult);
                        } finally {
                            collectionTimer.stop();
                        }
                        if(debugConfig.collection()) {
                            logger.info("Collected {} constraints of {}.", unitResult.getConstraints().size(), source);
                        }
                    }

                    {
                        final PartialSolution unitSolution;
                        if(incremental) {
                            if(debugConfig.resolution()) {
                                logger.info("Reducing {} constraints of {}.", unitResult.getConstraints().size(),
                                        source);
                            }
                            try {
                                solverTimer.start();
                                Function1<String, ITermVar> fresh =
                                        base -> TB.newVar(source, context.unit(source).fresh().fresh(base));
                                IMessageInfo messageInfo = ImmutableMessageInfo.of(MessageKind.ERROR,
                                        MessageContent.of(), Actions.sourceTerm(source));
                                unitSolution = Solver.solveIncremental(initialResult.getConfig(), globalTerms, fresh,
                                        unitResult.getConstraints(), messageInfo, progress.subProgress(1), cancel,
                                        debugConfig);
                                if(debugConfig.resolution()) {
                                    logger.info("Reduced file constraints to {}.",
                                            unitSolution.getResidualConstraints().size());
                                }
                            } catch(SolverException e) {
                                throw new AnalysisException(context, e);
                            } finally {
                                solverTimer.stop();
                            }
                        } else {
                            unitSolution = ImmutablePartialSolution.of(initialResult.getConfig(), globalTerms,
                                    unitResult.getConstraints(), new EmptyUnifier(), new EmptyMessages());
                        }
                        unit.setPartialSolution(unitSolution);
                        if(debugConfig.files() || debugConfig.resolution()) {
                            logger.info("Analyzed {}: {} errors, {} warnings, {} notes, {} unsolved constraints.",
                                    source, unitSolution.getMessages().getErrors().size(),
                                    unitSolution.getMessages().getWarnings().size(),
                                    unitSolution.getMessages().getNotes().size(),
                                    unitSolution.getResidualConstraints().size());
                        }
                    }

                } catch(MetaborgException e) {
                    logger.warn("Analysis of " + source + " failed.", e);
                    failures.put(source,
                            MessageFactory.newAnalysisErrorAtTop(parseUnit.source(), "File analysis failed.", e));
                }
            }

            // solve
            final Solution solution;
            final List<Optional<ITerm>> customUnits = Lists.newArrayList();
            {
                final List<PartialSolution> partialSolutions = Lists.newArrayList();
                Set<IConstraint> constraints =
                        context.initialResult().map(i -> i.getConstraints()).orElse(Collections.emptySet());
                for(IMultiFileScopeGraphUnit unit : context.units()) {
                    unit.partialSolution().ifPresent(partialSolutions::add);
                    unit.unitResult().map(UnitResult::getCustomResult).ifPresent(customUnits::add);
                }
                if(debugConfig.resolution()) {
                    logger.info("Solving {} project constraints + {} partial solutions.", constraints.size(),
                            partialSolutions.size());
                }
                try {
                    solverTimer.start();
                    Function1<String, ITermVar> fresh =
                            base -> TB.newVar(globalSource, context.unit(globalSource).fresh().fresh(base));
                    IMessageInfo messageInfo = ImmutableMessageInfo.of(MessageKind.ERROR, MessageContent.of(),
                            Actions.sourceTerm(globalSource));
                    solution = Solver.solveFinal(initialResult.getConfig(), fresh, constraints, partialSolutions,
                            messageInfo, progress.subProgress(w), cancel, debugConfig);
                } catch(SolverException e) {
                    throw new AnalysisException(context, e);
                } finally {
                    solverTimer.stop();
                }
                context.setSolution(solution);
                if(debugConfig.resolution()) {
                    logger.info("Project constraints solved.");
                }
            }

            // final
            FinalResult finalResult;
            final Optional<ITerm> customFinal;
            final Optional<CustomSolution> customSolution;
            {
                if(debugConfig.analysis()) {
                    logger.info("Finalizing project analysis.");
                }
                finalizeTimer.start();
                try {
                    ITerm finalResultTerm = doAction(strategy, Actions.analyzeFinal(globalSource), context, runtime)
                            .orElseThrow(() -> new AnalysisException(context, "No final result."));
                    finalResult = FinalResult.matcher().match(finalResultTerm)
                            .orElseThrow(() -> new AnalysisException(context, "Invalid final results."));
                    customFinal = doCustomAction(strategy, Actions.customFinal(globalSource,
                            customInitial.orElse(TB.EMPTY_TUPLE), TB.newList(Optionals.filter(customUnits))), context,
                            runtime);
                    finalResult = finalResult.withCustomResult(customFinal);
                    context.setFinalResult(finalResult);

                    customSolution = customFinal.flatMap(CustomSolution.matcher()::match);
                    customSolution.ifPresent(cs -> context.setCustomSolution(cs));
                } finally {
                    finalizeTimer.stop();
                }
                if(debugConfig.analysis()) {
                    logger.info("Project analysis finalized.");
                }
            }

            // errors
            {
                if(debugConfig.analysis()) {
                    logger.info("Processing project messages.");
                }
                Messages messages = new Messages();
                messages.addAll(Solver.unsolvedErrors(solution.getUnsolvedConstraints()));
                messages.addAll(solution.getMessages());
                customSolution.map(CustomSolution::getMessages).ifPresent(messages::addAll);
                IRelation3.Mutable<FileObject, MessageSeverity, IMessage> messagesByFile =
                        messagesByFile(Iterables.concat(failures.values(),
                                messages(messages.getAll(), solution.getUnifier(), context, context.location())));
                for(String source : changed.keySet()) {
                    final ISpoofaxParseUnit parseUnit = changed.get(source);
                    final FileObject file = parseUnit.source();
                    final Set<IMessage> fileMessages =
                            messagesByFile.get(file).stream().map(Map.Entry::getValue).collect(Collectors2.toHashSet());
                    fileMessages.addAll(ambiguitiesByFile.get(source));
                    final boolean valid = !failures.containsKey(source);
                    final boolean success = valid && messagesByFile.get(file, MessageSeverity.ERROR).isEmpty();
                    final IStrategoTerm analyzedAST = astsByFile.get(source);
                    results.add(unitService.analyzeUnit(changed.get(source),
                            new AnalyzeContrib(valid, success, analyzedAST != null, analyzedAST, fileMessages, -1),
                            context));
                    messagesByFile.remove(file);
                }
                for(FileObject file : messagesByFile.keySet()) {
                    final Set<IMessage> fileMessages =
                            messagesByFile.get(file).stream().map(Map.Entry::getValue).collect(Collectors2.toHashSet());
                    updateResults
                            .add(unitService.analyzeUnitUpdate(file, new AnalyzeUpdateData(fileMessages), context));
                }
                if(debugConfig.analysis() || debugConfig.files() || debugConfig.resolution()) {
                    logger.info("Analyzed {} files: {} errors, {} warnings, {} notes.", n, messages.getErrors().size(),
                            messages.getWarnings().size(), messages.getNotes().size());
                }
            }

        } catch(InterruptedException e) {
            logger.debug("Analysis was interrupted.");
        } finally {
            totalTimer.stop();
        }

        final ConstraintDebugData debugData = new ConstraintDebugData(totalTimer.stop(), collectionTimer.total(),
                solverTimer.total(), finalizeTimer.total());
        if(debugConfig.analysis()) {
            logger.info("{}", debugData);
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context, debugData);
    }

}
