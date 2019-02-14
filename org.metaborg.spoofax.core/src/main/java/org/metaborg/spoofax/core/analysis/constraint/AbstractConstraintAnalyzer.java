package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalyzeResults;
import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public abstract class AbstractConstraintAnalyzer implements ISpoofaxAnalyzer {

    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    protected final AnalysisCommon analysisCommon;
    protected final IResourceService resourceService;
    protected final IStrategoRuntimeService runtimeService;
    protected final IStrategoCommon strategoCommon;
    protected final ISpoofaxTracingService tracingService;
    protected final ISpoofaxUnitService unitService;

    protected final ITermFactory termFactory;

    public AbstractConstraintAnalyzer(final AnalysisCommon analysisCommon, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService,
            final ISpoofaxUnitService unitService) {
        this.analysisCommon = analysisCommon;
        this.resourceService = resourceService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.tracingService = tracingService;
        this.unitService = unitService;
        this.termFactory = termFactoryService.getGeneric();
    }

    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext, IProgress progress,
            ICancel cancel) throws AnalysisException {
        final ISpoofaxAnalyzeResults results =
                analyzeAll(Iterables2.singleton(input), genericContext, progress, cancel);
        if(results.results().isEmpty()) {
            throw new AnalysisException(genericContext, "Analysis failed.");
        }
        return new SpoofaxAnalyzeResult(Iterables.getOnlyElement(results.results()), results.updates(),
                results.context());
    }

    @Override public ISpoofaxAnalyzeResults analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext genericContext,
            IProgress progress, ICancel cancel) throws AnalysisException {
        IConstraintContext context;
        try {
            context = (IConstraintContext) genericContext;
        } catch(ClassCastException ex) {
            throw new AnalysisException(genericContext, "Constraint context required.", ex);
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

        final Map<String, ISpoofaxParseUnit> changed = Maps.newHashMap();
        final Map<String, ISpoofaxParseUnit> removed = Maps.newHashMap();
        for(ISpoofaxParseUnit input : inputs) {
            final String source =
                    input.detached() ? "detached-" + UUID.randomUUID().toString() : context.resourceKey(input.source());
            if(input.valid() && input.success() && !isEmptyAST(input.ast())) {
                changed.put(source, input);
            } else {
                removed.put(source, input);
            }
        }

        return analyzeAll(changed, removed, context, runtime, facet.strategyName, progress, cancel);
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return Tools.isTermTuple(ast) && ast.getSubtermCount() == 0;
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed,
            Map<String, ISpoofaxParseUnit> removed, IConstraintContext context, HybridInterpreter runtime,
            String strategy, IProgress progress, ICancel cancel) throws AnalysisException;

    protected boolean success(Collection<IMessage> messages) {
        return messages.stream().noneMatch(m -> m.severity().equals(MessageSeverity.ERROR));
    }

    protected IStrategoTerm build(String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    protected @Nullable List<IStrategoTerm> match(IStrategoTerm term, String op, int n) {
        if(term == null || !Tools.isTermAppl(term) || !Tools.hasConstructor((IStrategoAppl) term, op, n)) {
            return null;
        }
        return ImmutableList.copyOf(term.getAllSubterms());
    }

}
