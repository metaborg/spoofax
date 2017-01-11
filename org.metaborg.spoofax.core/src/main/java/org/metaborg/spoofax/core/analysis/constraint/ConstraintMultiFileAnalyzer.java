package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.vfs2.FileObject;
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

    public static final ILogger logger = LoggerUtils.logger(ConstraintMultiFileAnalyzer.class);

    public static final String name = "constraint-multifile";

    private final ISpoofaxUnitService unitService;
    private final IResourceService resourceService;

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.resourceService = resourceService;
        this.unitService = unitService;
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, IMultiFileScopeGraphContext context, HybridInterpreter runtime,
            String strategy) throws AnalysisException {
        String globalSource = context.location().getName().getURI();

        for(String input : removed.keySet()) {
            context.removeUnit(input);
        }

        // initial
        InitialResult initialResult;
        if(context.initialResult().isPresent()) {
            initialResult = context.initialResult().get();
        } else {
            ITerm initialResultTerm = doAction(strategy, Actions.analyzeInitial(globalSource), context, runtime)
                    .orElseThrow(() -> new AnalysisException(context, "No initial result."));
            initialResult = InitialResult.matcher().match(initialResultTerm)
                    .orElseThrow(() -> new AnalysisException(context, "Invalid initial results."));
            Optional<ITerm> customInitial =
                    doCustomAction(strategy, Actions.customInitial(globalSource), context, runtime);
            initialResult = ImmutableInitialResult.copyOf(initialResult).setCustomResult(customInitial);
            context.setInitialResult(initialResult);
        }

        // units
        final Map<String,IStrategoTerm> astsByFile = Maps.newHashMap();
        final Multimap<String,IMessage> ambiguitiesByFile = HashMultimap.create();
        final Multimap<String,IMessage> failuresByFile = HashMultimap.create();
        for(Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();
            ITerm ast = strategoTerms.fromStratego(parseUnit.ast());

            IMultiFileScopeGraphUnit unit = context.unit(source);
            unit.clear();

            try {
                ITerm unitResultTerm =
                        doAction(strategy, Actions.analyzeUnit(source, ast, initialResult.getArgs()), context, runtime)
                                .orElseThrow(() -> new AnalysisException(context, "No unit result."));
                UnitResult unitResult = UnitResult.matcher().match(unitResultTerm)
                        .orElseThrow(() -> new MetaborgException("Invalid unit results."));
                Optional<ITerm> customUnit = initialResult.getCustomResult().flatMap(initial -> {
                    return doCustomAction(strategy, Actions.customUnit(globalSource, ast, initial), context, runtime);
                });
                unitResult = ImmutableUnitResult.copyOf(unitResult).setCustomResult(customUnit);
                IStrategoTerm analyzedAST = strategoTerms.toStratego(unitResult.getAST());
                astsByFile.put(source, analyzedAST);
                ambiguitiesByFile.putAll(source, analysisCommon.ambiguityMessages(parseUnit.source(), analyzedAST));
                unit.setUnitResult(unitResult);
            } catch(MetaborgException e) {
                logger.warn("File analysis failed.", e);
                failuresByFile.put(source,
                        MessageFactory.newAnalysisErrorAtTop(parseUnit.source(), "File analysis failed.", e));
            }
        }

        // solve
        final List<Iterable<IConstraint>> constraints = Lists.newArrayList();
        final List<Optional<ITerm>> customUnits = Lists.newArrayList();
        context.initialResult().ifPresent(i -> constraints.add(i.getConstraints()));
        for(IMultiFileScopeGraphUnit unit : context.units()) {
            unit.unitResult().ifPresent(u -> {
                constraints.add(u.getConstraints());
                customUnits.add(u.getCustomResult());
            });
        }
        Solution solution;
        try {
            solution = Solver.solve(initialResult.getConfig(), Iterables.concat(constraints));
        } catch(UnsatisfiableException e) {
            throw new AnalysisException(context, e);
        }
        context.setSolution(solution);

        // final
        ITerm finalResultTerm = doAction(strategy, Actions.analyzeFinal(globalSource), context, runtime)
                .orElseThrow(() -> new AnalysisException(context, "No final result."));
        FinalResult finalResult = FinalResult.matcher().match(finalResultTerm)
                .orElseThrow(() -> new AnalysisException(context, "Invalid final results."));
        Optional<ITerm> customFinal = Optionals.lift(initialResult.getCustomResult(),
                Optionals.sequence(customUnits).map(us -> Lists.newArrayList(us)), (i, us) -> {
                    return (Optional<ITerm>) doCustomAction(strategy, Actions.customFinal(globalSource, i, us), context,
                            runtime);
                }).flatMap(o -> o);
        finalResult = ImmutableFinalResult.of().setCustomResult(customFinal);
        context.setFinalResult(finalResult);

        Optional<CustomSolution> customSolution = customFinal.flatMap(CustomSolution.matcher()::match);
        customSolution.ifPresent(cs -> context.setCustomSolution(cs));

        // errors
        Multimap<String,IMessage> errorsByFile = messagesByFile(
                merge(solution.getErrors(), customSolution.map(cs -> cs.getErrors()).orElse(Lists.newArrayList())),
                MessageSeverity.ERROR);
        Multimap<String,IMessage> warningsByFile = messagesByFile(
                merge(solution.getWarnings(), customSolution.map(cs -> cs.getWarnings()).orElse(Lists.newArrayList())),
                MessageSeverity.WARNING);
        Multimap<String,IMessage> notesByFile = messagesByFile(
                merge(solution.getNotes(), customSolution.map(cs -> cs.getNotes()).orElse(Lists.newArrayList())),
                MessageSeverity.NOTE);
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults = Lists.newArrayList();
        for(IMultiFileScopeGraphUnit unit : context.units()) {
            final String source = unit.resource();
            final Collection<IMessage> errors = errorsByFile.get(source);
            final Collection<IMessage> warnings = warningsByFile.get(source);
            final Collection<IMessage> notes = notesByFile.get(source);
            final Collection<IMessage> ambiguities = ambiguitiesByFile.get(source);
            final Collection<IMessage> messages =
                    Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
            messages.addAll(errors);
            messages.addAll(warnings);
            messages.addAll(notes);
            messages.addAll(ambiguities);
            if(changed.containsKey(source)) {
                results.add(unitService.analyzeUnit(changed.get(source),
                        new AnalyzeContrib(true, errors.isEmpty(), true, astsByFile.get(source), messages, -1),
                        context));
            } else {
                FileObject file = resourceService.resolve(source);
                updateResults.add(unitService.analyzeUnitUpdate(file, new AnalyzeUpdateData(messages), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

}