package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUnit;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.terms.index.TermIndex;
import org.metaborg.spoofax.core.terms.index.TermIndexCommon;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
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
        super(analysisCommon , runtimeService, strategoCommon, termFactoryService, tracingService);
        this.analysisCommon = analysisCommon;
        this.unitService = unitService;
    }


    @Override
    protected ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, IScopeGraphContext context,
            HybridInterpreter runtime, String strategy) throws AnalysisException {
        for(String input : removed.keySet()) {
            context.removeUnit(input);
        }

        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        for(Map.Entry<String,ISpoofaxParseUnit> input : changed.entrySet()) {
            String source = input.getKey();
            ISpoofaxParseUnit parseUnit = input.getValue();

            IScopeGraphUnit unit = new ScopeGraphUnit(source, parseUnit);
            context.addUnit(unit);
            
            IStrategoTerm globalTerm = makeAppl("Global");
            TermIndex.put(globalTerm, source, 0);

            IStrategoTerm globalConstraint = initialize(source, globalTerm,
                    strategy, context, runtime);
            IStrategoTerm globalParams = unit.metadata(0, paramsKey);
            IStrategoTerm globalType = unit.metadata(0, typeKey);
            IStrategoTerm fullParams = globalType == null ?
                    globalParams : termFactory.makeTuple(globalParams, globalType);
 
            final IStrategoTerm desugared = preprocessAST(source, parseUnit.ast(),
                    strategy, context, runtime);
            TermIndexCommon.index(source, desugared);
            final IStrategoTerm fileConstraint = generateConstraint(source, desugared,
                    fullParams, strategy, context, runtime);
            unit.setConstraint(fileConstraint);
            final IStrategoTerm constraint = conj(Lists.newArrayList(globalConstraint, fileConstraint));

            final IStrategoTerm result = solveConstraint(constraint, strategy, context, runtime);
            for(IStrategoTerm entry : result.getSubterm(3)) {
                TermIndex index = TermIndex.TYPE.fromTerm((IStrategoAppl) entry.getSubterm(0));
                unit.addNameResolution(index.nodeId(), entry.getSubterm(1));
            }
            unit.setAnalysis(result.getSubterm(4));

            final Collection<IMessage> errors =
                    analysisCommon.messages(parseUnit.source(), MessageSeverity.ERROR, result.getSubterm(0));
            final Collection<IMessage> warnings =
                    analysisCommon.messages(parseUnit.source(), MessageSeverity.WARNING, result.getSubterm(1));
            final Collection<IMessage> notes =
                    analysisCommon.messages(parseUnit.source(), MessageSeverity.NOTE, result.getSubterm(2));
            final Collection<IMessage> ambiguities =
                    analysisCommon.ambiguityMessages(parseUnit.source(), parseUnit.ast());
            final Collection<IMessage> messages =
                Lists.newArrayListWithCapacity(errors.size() + warnings.size() + notes.size() + ambiguities.size());
            messages.addAll(errors);
            messages.addAll(warnings);
            messages.addAll(notes);
            messages.addAll(ambiguities);

            results.add(unitService.analyzeUnit(parseUnit,
                    new AnalyzeContrib(true, errors.isEmpty(), true,
                            desugared, messages, -1), context));
        }
        return new SpoofaxAnalyzeResults(results,
                Collections.<ISpoofaxAnalyzeUnitUpdate>emptyList(), context);
    }

}