package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphInitial;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUnit;
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
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-multifile";

    private final AnalysisCommon analysisCommon;
    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintMultiFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService  termFactoryService,
            final ISpoofaxTracingService tracingService
    ) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService,
                tracingService);
        this.analysisCommon = analysisCommon;
        this.unitService = unitService;
    }


    @Override
    protected ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            ScopeGraphContext context, HybridInterpreter runtime, String strategy,
            ITermFactory termFactory) throws AnalysisException {
        IScopeGraphInitial initial;
        if((initial = context.initial()) == null) {
            initial = initialize(strategy, context, runtime, termFactory);
            context.setInitial(initial);
        }
        final Map<FileObject,ISpoofaxParseUnit> inputsByFile = Maps.newHashMap();
        final Map<FileObject,IStrategoTerm> astsByFile = Maps.newHashMap();
        final Multimap<FileObject,IMessage> ambiguitiesByFile = HashMultimap.create();
        for(ISpoofaxParseUnit input : inputs) {
            inputsByFile.put(input.source(), input);
            IStrategoTerm desugared = preprocessAST(input.ast(), strategy, context,
                    runtime, termFactory);
            IStrategoTerm indexed = indexAST(desugared, strategy, context, runtime, termFactory);
            IStrategoTerm fileConstraint = generateConstraint(indexed, initial.params(),
                    strategy, context, runtime, termFactory);
            astsByFile.put(input.source(), indexed);
            ambiguitiesByFile.putAll(input.source(), analysisCommon.ambiguityMessages(input.source(), input.ast()));
            context.addUnit(new ScopeGraphUnit(input.source(), fileConstraint));
        }

        final Collection<IStrategoTerm> constraints =
                Lists.newArrayList(initial.constraint());
        for(IScopeGraphUnit unit : context.units().values()) {
            constraints.add(unit.constraint());
        }
        IStrategoTerm constraint = normalizeConstraint(conj(constraints, termFactory),
                strategy, context, runtime, termFactory);
        IStrategoTerm result = solveConstraint(constraint, strategy, context, runtime, termFactory);
        Multimap<FileObject,IMessage> errorsByFile = messages(result.getSubterm(0), MessageSeverity.ERROR);
        Multimap<FileObject,IMessage> warningsByFile = messages(result.getSubterm(1), MessageSeverity.WARNING);
        Multimap<FileObject,IMessage> notesByFile = messages(result.getSubterm(2), MessageSeverity.NOTE);
        context.setAnalysis(result.getSubterm(3));

        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults =
            Lists.newArrayList();
        for(IScopeGraphUnit unit : context.units().values()) {
            final FileObject source = unit.source();
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
            if(inputsByFile.containsKey(source)) {
                results.add(unitService.analyzeUnit(
                        inputsByFile.get(source),
                        new AnalyzeContrib(true, errors.isEmpty(), true, astsByFile.get(source),
                                false, null, messages, -1), context));
            } else {
                // GTODO : set success value here too
                updateResults.add(unitService.analyzeUnitUpdate(source,
                        new AnalyzeUpdateData(messages), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

}
