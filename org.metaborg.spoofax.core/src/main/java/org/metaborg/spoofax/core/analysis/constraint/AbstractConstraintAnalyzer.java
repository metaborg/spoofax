package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Map;
import java.util.UUID;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.scopegraph.ISpoofaxScopeGraphContext;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

abstract class AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {
    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    protected final AnalysisCommon analysisCommon;
    protected final IStrategoRuntimeService runtimeService;
    protected final IStrategoCommon strategoCommon;
    protected final ISpoofaxTracingService tracingService;

    protected final ITermFactory termFactory;
    protected final IStrategoConstructor analyzeInitial;
    protected final IStrategoConstructor analyzeUnit;
    protected final IStrategoConstructor analyzeFinal;
    protected final IStrategoConstructor applyAnalysis;

    public AbstractConstraintAnalyzer(
            final AnalysisCommon analysisCommon,
            final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService
    ) {
        this.analysisCommon = analysisCommon;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.tracingService = tracingService;
        termFactory = termFactoryService.getGeneric();
        analyzeInitial = termFactory.makeConstructor("AnalyzeInitial", 1);
        analyzeUnit = termFactory.makeConstructor("AnalyzeUnit", 3);
        analyzeFinal = termFactory.makeConstructor("AnalyzeFinal", 3);
        applyAnalysis = termFactory.makeConstructor("ApplyAnalysis", 2);
    }

    @Override
    public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext) throws AnalysisException {
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(genericContext, message);
        }
        final ISpoofaxAnalyzeResults results = analyzeAll(Iterables2.singleton(input), genericContext);
        if(results.results().isEmpty()) {
            throw new AnalysisException(genericContext,"Analysis failed.");
        }
        return new SpoofaxAnalyzeResult(Iterables.getOnlyElement(results.results()),
                results.updates(), results.context());
    }

    @Override
    public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs,
            IContext genericContext) throws AnalysisException {
        ISpoofaxScopeGraphContext context;
        try {
            context = (ISpoofaxScopeGraphContext) genericContext;
        } catch(ClassCastException ex) {
            throw new AnalysisException(genericContext,"Scope graph context required for constraint analysis.",ex);
        }

        final ILanguageImpl langImpl = context.language();
 
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
 
        Map<String,ISpoofaxParseUnit> changed = Maps.newHashMap();
        Map<String,ISpoofaxParseUnit> removed = Maps.newHashMap();
        for(ISpoofaxParseUnit input : inputs) {
            String source = input.detached() ? ("detached-"+UUID.randomUUID().toString()) : input.source().getName().getURI();
            (input.valid() ? changed : removed).put(source,input);
        }
        return analyzeAll(changed, removed, context, runtime, facet.strategyName);
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, ISpoofaxScopeGraphContext context,
            HybridInterpreter runtime, String strategy) throws AnalysisException;

    protected IStrategoTerm doAction(String strategy, IStrategoTerm action,
            ISpoofaxScopeGraphContext context, HybridInterpreter runtime) throws AnalysisException {
        try {
            IStrategoTerm result = strategoCommon.invoke(runtime, action, strategy);
            if(result == null) {
                throw new MetaborgException("Analysis strategy failed.");
            }
            return result;
        } catch (MetaborgException ex) {
            final String message = "Analysis failed.\n"+ex.getMessage();
            throw new AnalysisException(context,message,ex);
        }
    }
 
    protected Multimap<String,IMessage> messages(IStrategoTerm messageList, MessageSeverity severity) {
        Multimap<String,IMessage> messages = HashMultimap.create();
        for(IStrategoTerm messageTerm : messageList) {
            if(messageTerm.getSubtermCount() != 2) {
                logger.error("Analysis message has wrong format, skipping: {}", messageTerm);
                continue;
            }
            final IStrategoTerm originTerm = messageTerm.getSubterm(0);
            final String message = messageTerm.getSubterm(1).toString();
            final ISourceLocation location = tracingService.location(originTerm);
            if(location == null) {
                logger.error("Analysis message has no origin, skipping: {}", messageTerm);
                continue;
            }
            messages.put(location.resource().getName().getURI(),
                    MessageFactory.newAnalysisMessage(location.resource(),
                            location.region(), message, severity, null));
        }
        return messages;
    }
 
    protected void applySolution(Table<Integer,IStrategoTerm,IStrategoTerm> data,
            IStrategoTerm solution, String strategy, ISpoofaxScopeGraphContext context,
            HybridInterpreter runtime) throws AnalysisException {
        for(Integer i : data.rowKeySet()) {
            for(IStrategoTerm key : data.columnKeySet()) {
                IStrategoTerm value;
                if((value = data.get(i, key)) != null) {
                    IStrategoTerm action = termFactory.makeAppl(applyAnalysis, value, solution);
                    value = doAction(strategy, action, context, runtime);
                    data.put(i, key, value);
                }
            }
        }
    }
    
}