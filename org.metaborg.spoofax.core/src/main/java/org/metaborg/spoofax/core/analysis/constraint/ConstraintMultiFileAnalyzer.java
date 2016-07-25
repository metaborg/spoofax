package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
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
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class ConstraintMultiFileAnalyzer extends AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    public static final String name = "constraint-multifile";

    private final ISpoofaxUnitService unitService;

    @Inject public ConstraintMultiFileAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService  termFactoryService
    ) {
        super(analysisCommon, runtimeService, strategoCommon, termFactoryService);
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
        for(ISpoofaxParseUnit input : inputs) {
            inputsByFile.put(input.source(), input);
            IStrategoTerm desugared = preprocessAST(input.ast(), strategy, context,
                    runtime, termFactory);
            IStrategoTerm indexed = indexAST(desugared, strategy, context, runtime, termFactory);
            IStrategoTerm fileConstraint = generateConstraint(indexed, initial.params(),
                    strategy, context, runtime, termFactory);
            astsByFile.put(input.source(), indexed);
            context.addUnit(new ScopeGraphUnit(input.source(), fileConstraint));
        }

        final Collection<IStrategoTerm> constraints =
                Lists.newArrayList(initial.constraint());
        for(IScopeGraphUnit unit : context.units()) {
            constraints.add(unit.constraint());
        }
        IStrategoTerm constraint = conj(constraints, termFactory);
        IStrategoTerm solution = solveConstraint(constraint, strategy, context, runtime, termFactory);
        // generate error messages from solver
        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults =
            Lists.newArrayList();
        for(IScopeGraphUnit unit : context.units()) {
            if(inputsByFile.containsKey(unit.source())) {
                IMessage message = MessageFactory.newAnalysisNoteAtTop(
                        unit.source(), "Multi file primary analysis.", null);
                results.add(unitService.analyzeUnit(
                        inputsByFile.get(unit.source()),
                        new AnalyzeContrib(true, true, true, astsByFile.get(unit.source()),
                                false, null, Iterables2.singleton(message), -1), context));
                return new SpoofaxAnalyzeResults(results,
                        Collections.<ISpoofaxAnalyzeUnitUpdate>emptyList(), context);
            } else {
                IMessage message = MessageFactory.newAnalysisErrorAtTop(unit.source(),
                        "Multi file secondary analysis.", null);
                updateResults.add(unitService.analyzeUnitUpdate(unit.source(),
                        new AnalyzeUpdateData(Iterables2.singleton(message)), context));
            }
        }

        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

}
