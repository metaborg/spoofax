package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphInitial;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ConstraintSingleFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-singlefile";

    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintSingleFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService
    ) {
        super(analysisCommon , runtimeService, strategoCommon, termFactoryService);
        this.unitService = unitService;
    }


    @Override
    protected ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            ScopeGraphContext context, HybridInterpreter runtime, String strategy,
            ITermFactory termFactory) throws AnalysisException {
        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        for(ISpoofaxParseUnit input : inputs) {
            ScopeGraphInitial initial = initialize(strategy, context, runtime, termFactory);
            IStrategoTerm desugared = preprocessAST(input.ast(), strategy, context,
                    runtime, termFactory);
            IStrategoTerm indexed = indexAST(desugared, strategy, context, runtime, termFactory);
            IStrategoTerm fileConstraint = generateConstraint(indexed, initial.params(),
                    strategy, context, runtime, termFactory);
            IStrategoTerm constraint =
                    conj(Lists.newArrayList(initial.constraint(), fileConstraint), termFactory);
            IStrategoTerm solution = solveConstraint(constraint, strategy, context, runtime, termFactory);
            // generate error messages from solver
            IMessage message = MessageFactory.newAnalysisNoteAtTop(
                    input.source(), "Single file analysis.", null);
            results.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(true, true, true, input.ast(),
                            false, null, Iterables2.singleton(message), -1), context));
        }
        return new SpoofaxAnalyzeResults(results,
                Collections.<ISpoofaxAnalyzeUnitUpdate>emptyList(), context);
    }

}
