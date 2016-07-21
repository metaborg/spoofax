package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.context.ScopeGraphContext;
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
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Lists;

public abstract class AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    private static final ILogger logger = LoggerUtils.logger(TaskEngineAnalyzer.class);

    private static final String INIT_STRATEGY = "generate-constraint-init";
    private static final String MATCH_STRATEGY = "generate-constraint";
    
    private final AnalysisCommon analysisCommon;
    private final IStrategoRuntimeService runtimeService;
    private final ITermFactoryService termFactoryService;
    private final ISpoofaxUnitService unitService;
    private final IStrategoCommon strategoCommon;
    private final boolean multifile;

    public AbstractConstraintAnalyzer(
            final AnalysisCommon analysisCommon,
            final ISpoofaxUnitService unitService,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService,
            final boolean multifile
    ) {
        this.analysisCommon = analysisCommon;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.termFactoryService = termFactoryService;
        this.unitService = unitService;
        this.multifile = multifile;
    }

    @Override
    public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext context) throws AnalysisException {
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(context, message);
        }
        final ISpoofaxAnalyzeResults results = analyzeAll(Iterables2.singleton(input), context);
        return new SpoofaxAnalyzeResult(results.results().iterator().next(), results.updates(), context);
    }

    @Override
    public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            IContext context) throws AnalysisException {
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

    private ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            IContext genericContext, HybridInterpreter runtime, String strategy,
            ITermFactory termFactory) throws AnalysisException {
        ScopeGraphContext context;
        try {
            context = (ScopeGraphContext) genericContext;
        } catch(ClassCastException ex) {
            throw new AnalysisException(genericContext,"Scope graph context required for constraint analysis.",ex);
        }
 
        initialize(context, runtime, termFactory);
        
        final Collection<ISpoofaxAnalyzeUnit> results =
            Lists.newArrayList();
        for(ISpoofaxParseUnit input : inputs) {
            context.sources().remove(input.source());
            IMessage message = MessageFactory.newAnalysisWarningAtTop(input.source(), "First warning.", null);
            results.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(true, true, true, input.ast(),
                            false, null, Iterables2.singleton(message), -1), context));
        }
        final Collection<ISpoofaxAnalyzeUnitUpdate> updateResults =
            Lists.newArrayList();
        for(FileObject source : context.sources()) {
            IMessage message = MessageFactory.newAnalysisErrorAtTop(source, "Two strikes, out.", null);
            updateResults.add(unitService.analyzeUnitUpdate(source,
                    new AnalyzeUpdateData(Iterables2.singleton(message)), context));
        }
        for(ISpoofaxParseUnit input : inputs) {
            context.sources().add(input.source());
        }
    
        return new SpoofaxAnalyzeResults(results, updateResults, context);
    }

    private void initialize(ScopeGraphContext context, HybridInterpreter runtime,
            ITermFactory termFactory) throws AnalysisException {
        if(context.getInitial() == null) {
            try {
                IStrategoTerm input = termFactory.makeString("init");
                IStrategoTerm initial = strategoCommon.invoke(runtime, input, INIT_STRATEGY);
                context.setInitial(initial);
            } catch (MetaborgException ex) {
                final String message = analysisCommon.analysisFailedMessage(runtime);
                throw new AnalysisException(context,message,ex);
            }
        }
    }
    
}