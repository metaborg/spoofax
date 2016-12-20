package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.source.ISourceLocation;
import org.metaborg.meta.nabl2.solver.Message;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.util.Unit;
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
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

abstract class AbstractConstraintAnalyzer<C extends ISpoofaxScopeGraphContext<?>> implements ISpoofaxAnalyzer {

    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    protected final AnalysisCommon analysisCommon;
    protected final IStrategoRuntimeService runtimeService;
    protected final IStrategoCommon strategoCommon;
    protected final ISpoofaxTracingService tracingService;

    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;

    public AbstractConstraintAnalyzer(final AnalysisCommon analysisCommon, final IStrategoRuntimeService runtimeService,
            final IStrategoCommon strategoCommon, final ITermFactoryService termFactoryService,
            final ISpoofaxTracingService tracingService) {
        this.analysisCommon = analysisCommon;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.tracingService = tracingService;
        this.termFactory = termFactoryService.getGeneric();
        this.strategoTerms = new StrategoTerms(termFactory,
                // @formatter:off
                (term, attacher) -> {
                    ISourceLocation location = tracingService.location(term);
                    if(location != null) {
                        attacher.put(ISourceLocation.class, location);
                    }
                    return Unit.unit;
                }
        // @formatter:on
        );
    }

    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext)
            throws AnalysisException {
        if (!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(genericContext, message);
        }
        final ISpoofaxAnalyzeResults results = analyzeAll(Iterables2.singleton(input), genericContext);
        if (results.results().isEmpty()) {
            throw new AnalysisException(genericContext, "Analysis failed.");
        }
        return new SpoofaxAnalyzeResult(Iterables.getOnlyElement(results.results()), results.updates(), results
                .context());
    }

    @SuppressWarnings("unchecked") @Override public ISpoofaxAnalyzeResults analyzeAll(
            Iterable<ISpoofaxParseUnit> inputs, IContext genericContext) throws AnalysisException {
        C context;
        try {
            context = (C) genericContext;
        } catch (ClassCastException ex) {
            throw new AnalysisException(genericContext, "Scope graph context required for constraint analysis.", ex);
        }

        final ILanguageImpl langImpl = context.language();

        final FacetContribution<AnalysisFacet> facetContribution = langImpl.facetContribution(AnalysisFacet.class);
        if (facetContribution == null) {
            logger.debug("No analysis required for {}", langImpl);
            return new SpoofaxAnalyzeResults(context);
        }
        final AnalysisFacet facet = facetContribution.facet;

        final HybridInterpreter runtime;
        try {
            runtime = runtimeService.runtime(facetContribution.contributor, context, false);
        } catch (MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }

        Map<String,ISpoofaxParseUnit> changed = Maps.newHashMap();
        Map<String,ISpoofaxParseUnit> removed = Maps.newHashMap();
        for (ISpoofaxParseUnit input : inputs) {
            String source = input.detached() ? ("detached-" + UUID.randomUUID().toString())
                    : input.source().getName().getURI();
            (input.valid() ? changed : removed).put(source, input);
        }
        return analyzeAll(changed, removed, context, runtime, facet.strategyName);
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String,ISpoofaxParseUnit> changed,
            Map<String,ISpoofaxParseUnit> removed, C context, HybridInterpreter runtime, String strategy)
            throws AnalysisException;

    protected Optional<ITerm> doAction(String strategy, ITerm action, ISpoofaxScopeGraphContext<?> context,
            HybridInterpreter runtime) throws AnalysisException {
        try {
            return Optional.ofNullable(strategoTerms.fromStratego(strategoCommon.invoke(runtime, strategoTerms
                    .toStratego(action), strategy)));
        } catch (MetaborgException ex) {
            final String message = "Analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }

    protected Optional<ITerm> doCustomAction(String strategy, ITerm action, ISpoofaxScopeGraphContext<?> context,
            HybridInterpreter runtime) {
        try {
            return doAction(strategy, action, context, runtime);
        } catch (Exception ex) {
            logger.warn("Custom analysis step failed.", ex);
            return Optional.empty();
        }
    }

    protected Multimap<String,IMessage> messagesByFile(List<Message> messages, MessageSeverity severity) {
        Multimap<String,IMessage> imessages = HashMultimap.create();
        for (Message message : messages) {
            IMessage imessage = message(message.getOrigin(), message.getMessage(), severity);
            if (imessage != null) {
                imessages.put(imessage.source().getName().getURI(), imessage);
            }
        }
        return imessages;
    }

    protected Collection<IMessage> messages(FileObject resource, List<Message> messages, MessageSeverity severity) {
        List<IMessage> imessages = Lists.newArrayList();
        for (Message message : messages) {
            IMessage imessage = message(message.getOrigin(), message.getMessage(), severity);
            if (imessage != null) {
                imessages.add(imessage);
            }
        }
        return imessages;
    }

    protected IMessage message(ITerm originatingTerm, String message, MessageSeverity severity) {
        ISourceLocation location = originatingTerm.getAttachments().getInstance(ISourceLocation.class);
        if (location != null) {
            return MessageFactory.newAnalysisMessage(location.resource(), location.region(), message, severity, null);
        } else {
            logger.warn("Ignoring location-less {}: {}", severity, message);
            return null;
        }
    }

    protected List<Message> merge(List<Message> m1, List<Message> m2) {
        List<Message> m = Lists.newArrayList();
        m.addAll(m1);
        m.addAll(m2);
        return  m;
    }
    
}