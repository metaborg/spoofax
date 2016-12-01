package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.meta.nabl2.ScopeGraphException;
import org.metaborg.meta.nabl2.constraints.IConstraint;
import org.metaborg.meta.nabl2.solver.Solution;
import org.metaborg.meta.nabl2.solver.Solver;
import org.metaborg.meta.nabl2.solver.UnsatisfiableException;
import org.metaborg.meta.nabl2.spoofax.Actions;
import org.metaborg.meta.nabl2.spoofax.FinalResult;
import org.metaborg.meta.nabl2.spoofax.InitialResult;
import org.metaborg.meta.nabl2.spoofax.Results;
import org.metaborg.meta.nabl2.spoofax.UnitResult;
import org.metaborg.meta.nabl2.stratego.StrategoCommon;
import org.metaborg.meta.nabl2.stratego.StrategoConstraints;
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
    private final Results resultBuilder;
    private final Actions actionBuilder;

    @Inject public ConstraintMultiFileAnalyzer(final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService, tracingService);
        this.resourceService = resourceService;
        this.unitService = unitService;
        this.resultBuilder = new Results(new StrategoConstraints(new StrategoCommon(termFactory, strategoTermFactory)));
        this.actionBuilder = new Actions(strategoTermFactory);
    }

    @Override protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, IMultiFileScopeGraphContext context, HybridInterpreter runtime,
            String strategy) throws AnalysisException {
        String globalSource = context.location().getName().getURI();

        for (String input : removed.keySet()) {
            context.removeUnit(input);
        }

        // initial
        InitialResult initialResult;
        if (context.initialResult().isPresent()) {
            initialResult = context.initialResult().get();
        } else {
            IStrategoTerm initialResultTerm = doAction(strategy, actionBuilder.initialOf(globalSource), context,
                    runtime);
            try {
                initialResult = resultBuilder.initialOf(initialResultTerm);
            } catch (ScopeGraphException e) {
                throw new AnalysisException(context, e);
            }
            context.setInitialResult(initialResult);
        }

        // units
        final Map<String,IStrategoTerm> astsByFile = Maps.newHashMap();
        final Multimap<String,IMessage> ambiguitiesByFile = HashMultimap.create();
        final List<Iterable<IConstraint>> constraints = Lists.newArrayList();
        final Multimap<String,IMessage> failuresByFile = HashMultimap.create();
        for (Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();

            IMultiFileScopeGraphUnit unit = context.unit(source);
            unit.clear();

            try {
                IStrategoTerm unitResultTerm = doAction(strategy, actionBuilder.unitOf(source, parseUnit.ast(),
                        initialResult.getParams(), initialResult.getType()), context, runtime);
                UnitResult unitResult = resultBuilder.unitOf(unitResultTerm);
                constraints.add(unitResult.getConstraints());
                astsByFile.put(source, unitResult.getAST());
                ambiguitiesByFile.putAll(source,
                        analysisCommon.ambiguityMessages(parseUnit.source(), unitResult.getAST()));
                unit.setUnitResult(unitResult);
            } catch (MetaborgException | ScopeGraphException e) {
                logger.warn("File analysis failed.", e);
                failuresByFile.put(source,
                        MessageFactory.newAnalysisErrorAtTop(parseUnit.source(), "File analysis failed.", e));
            }
        }

        // solve
        Solution solution;
        try {
            solution = Solver.solve(Iterables.concat(constraints), termFactory);
        } catch (UnsatisfiableException e) {
            throw new AnalysisException(context, e);
        }
        context.setSolution(solution);

        // final
        IStrategoTerm finalResultTerm = doAction(strategy, actionBuilder.finalOf(globalSource), context, runtime);
        FinalResult finalResult;
        try {
            finalResult = resultBuilder.finalOf(finalResultTerm);
        } catch (ScopeGraphException e) {
            throw new AnalysisException(context, e);
        }
        context.setFinalResult(finalResult);

        // errors
        Multimap<String,IMessage> errorsByFile = messagesByFile(solution.getErrors(), MessageSeverity.ERROR);
        Multimap<String,IMessage> warningsByFile = messagesByFile(solution.getWarnings(), MessageSeverity.WARNING);
        Multimap<String,IMessage> notesByFile = messagesByFile(solution.getNotes(), MessageSeverity.NOTE);
        final Collection<ISpoofaxAnalyzeUnit> results = Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults = Lists.newArrayList();
        for (IMultiFileScopeGraphUnit unit : context.units()) {
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
            if (changed.containsKey(source)) {
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