package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphInitial;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConstraintSingleFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-singlefile";

    private final AnalysisCommon analysisCommon;
    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintSingleFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService
    ) {
        super(analysisCommon , runtimeService, strategoCommon, termFactoryService,
                tracingService);
        this.analysisCommon = analysisCommon;
        this.unitService = unitService;
    }


    @Override
    protected ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            ScopeGraphContext context, HybridInterpreter runtime, String strategy,
            ITermFactory termFactory) throws AnalysisException {
        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        for(ISpoofaxParseUnit input : inputs) {
            final ScopeGraphInitial initial = initialize(strategy, context, runtime, termFactory);
            final IStrategoTerm desugared = preprocessAST(input.ast(), strategy, context,
                    runtime, termFactory);
            final IStrategoTerm indexed = indexAST(desugared, strategy, context, runtime, termFactory);
            final IStrategoTerm fileConstraint = generateConstraint(indexed, initial.params(),
                    strategy, context, runtime, termFactory);
            final IStrategoTerm constraint =
                    conj(Lists.newArrayList(initial.constraint(), fileConstraint), termFactory);

            final IStrategoTerm result = solveConstraint(constraint, strategy, context, runtime, termFactory);
            final Collection<IMessage> errors =
                    analysisCommon.messages(input.source(), MessageSeverity.ERROR, result.getSubterm(0));
            final Collection<IMessage> warnings =
                    analysisCommon.messages(input.source(), MessageSeverity.WARNING, result.getSubterm(1));
            final Collection<IMessage> notes =
                    analysisCommon.messages(input.source(), MessageSeverity.NOTE, result.getSubterm(2));
            final Collection<IMessage> ambiguities = analysisCommon.ambiguityMessages(input.source(), input.ast());
            final Collection<IMessage> messages =
                Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
            messages.addAll(errors);
            messages.addAll(warnings);
            messages.addAll(notes);
            messages.addAll(ambiguities);

            context.addUnit(new ScopeGraphUnit(input.source(), fileConstraint, result.getSubterm(3)));
            results.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(true, true, true, input.ast(),
                            false, null, messages, -1), context));
        }
        return new SpoofaxAnalyzeResults(results,
                Collections.<ISpoofaxAnalyzeUnitUpdate>emptyList(), context);
    }

}
