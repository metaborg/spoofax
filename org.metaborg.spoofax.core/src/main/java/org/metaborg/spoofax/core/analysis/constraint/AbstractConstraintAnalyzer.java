package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphInitial;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public abstract class AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    private static final ILogger logger = LoggerUtils.logger(TaskEngineAnalyzer.class);

    private static final String CONJ = "CConj";

    private static final String INIT_ACTION = "Init";
    private static final String INDEX_AST_ACTION = "IndexAST";
    private static final String PREPROCESS_AST_ACTION = "PreprocessAST";
    private static final String GENERATE_CONSTRAINT_ACTION = "GenerateConstraint";
    private static final String SOLVE_CONSTRAINT_ACTION = "SolveConstraint";
    private static final String POSTPROCESS_AST_ACTION = "PostprocessAST";

    private final AnalysisCommon analysisCommon;
    private final IStrategoRuntimeService runtimeService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoCommon strategoCommon;

    public AbstractConstraintAnalyzer(
            final AnalysisCommon analysisCommon,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService
    ) {
        this.analysisCommon = analysisCommon;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.termFactoryService = termFactoryService;
    }

    @Override
    public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext) throws AnalysisException {
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(genericContext, message);
        }
        final ISpoofaxAnalyzeResults results = analyzeAll(Iterables2.singleton(input), genericContext);
        return new SpoofaxAnalyzeResult(Iterables.getOnlyElement(results.results()),
                results.updates(), results.context());
    }

    @Override
    public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            IContext genericContext) throws AnalysisException {
        ScopeGraphContext context;
        try {
            context = (ScopeGraphContext) genericContext;
        } catch(ClassCastException ex) {
            throw new AnalysisException(genericContext,"Scope graph context required for constraint analysis.",ex);
        }

        final ILanguageImpl langImpl = context.language();
        final ITermFactory termFactory = termFactoryService.getGeneric();
    
        final FacetContribution<AnalysisFacet> facetContribution = langImpl.facetContribution(AnalysisFacet.class);
        if(facetContribution == null) {
            logger.debug("No analysis required for {}", langImpl);
            return new SpoofaxAnalyzeResults(context);
        }
        final AnalysisFacet facet = facetContribution.facet;
        
        final HybridInterpreter runtime;
        try {
            runtime = runtimeService.runtime(facetContribution.contributor, context, false);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }
    
        return analyzeAll(inputs, context, runtime, facet.strategyName, termFactory);
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            ScopeGraphContext genericContext, HybridInterpreter runtime, String strategy,
            ITermFactory termFactory) throws AnalysisException;

    protected ScopeGraphInitial initialize(String strategy, ScopeGraphContext context,
            HybridInterpreter runtime, ITermFactory termFactory) throws AnalysisException {
        IStrategoTerm paramsAndConstraint = doAction(INIT_ACTION, strategy, context, runtime, termFactory);
        return new ScopeGraphInitial(paramsAndConstraint.getSubterm(0),
                paramsAndConstraint.getSubterm(1));
    }
 
    protected IStrategoTerm preprocessAST(IStrategoTerm ast, String strategy,
            ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory) throws AnalysisException {
        return doAction(PREPROCESS_AST_ACTION, strategy, context, runtime, termFactory, ast);
    }

    protected IStrategoTerm indexAST(IStrategoTerm ast, String strategy,
            ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory) throws AnalysisException {
        return doAction(INDEX_AST_ACTION, strategy, context, runtime, termFactory, ast);
    }

    protected IStrategoTerm generateConstraint(IStrategoTerm ast, IStrategoTerm params,
            String strategy, ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory) throws AnalysisException {
        return doAction(GENERATE_CONSTRAINT_ACTION, strategy, context, runtime, termFactory, ast,params);
    }
 
    protected IStrategoTerm solveConstraint(IStrategoTerm constraint, String strategy,
            ScopeGraphContext context, HybridInterpreter runtime, ITermFactory termFactory)
                    throws AnalysisException {
        return doAction(SOLVE_CONSTRAINT_ACTION, strategy, context, runtime, termFactory, constraint);
    }
 
    protected IStrategoTerm postpocessAST(IStrategoTerm ast, String strategy,
            ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory) throws AnalysisException {
        return doAction(POSTPROCESS_AST_ACTION, strategy, context, runtime, termFactory, ast);
    }

    private IStrategoTerm doAction(String actionName, String strategy,
            ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory,  IStrategoTerm... args) throws AnalysisException {
        IStrategoTerm action = termFactory.makeAppl(termFactory.makeConstructor(actionName, 0));
        IStrategoTerm[] argTerms = Lists.asList(action, args).toArray(new IStrategoTerm[0]);
        IStrategoTerm argTerm;
        if(args.length == 0) {
            argTerm = action;
        } else {
            argTerm = termFactory.makeTuple(argTerms);
        }
        try {
            IStrategoTerm result = strategoCommon.invoke(runtime, argTerm, strategy);
            if(result == null) {
                throw new MetaborgException("Analysis strategy failed.");
            }
            return result;
        } catch (MetaborgException ex) {
            final String message = analysisCommon.analysisFailedMessage(runtime);
            throw new AnalysisException(context,message,ex);
        }
    }
 
    protected IStrategoTerm conj(Collection<IStrategoTerm> constraints, ITermFactory termFactory) {
        IStrategoConstructor conj = termFactory.makeConstructor(CONJ, 1);
        return termFactory.makeAppl(conj, termFactory.makeList(constraints));
    }
    
}