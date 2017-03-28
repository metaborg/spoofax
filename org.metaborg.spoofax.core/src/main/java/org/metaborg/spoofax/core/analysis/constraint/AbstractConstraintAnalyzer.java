package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.spoofax.analysis.EditorMessage;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.stratego.TermOrigin;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.unification.IUnifier;
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
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

abstract class AbstractConstraintAnalyzer<C extends ISpoofaxScopeGraphContext<?>> implements ISpoofaxAnalyzer {

    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    protected final AnalysisCommon analysisCommon;
    protected final IResourceService resourceService;
    protected final IStrategoRuntimeService runtimeService;
    protected final IStrategoCommon strategoCommon;
    protected final ISpoofaxTracingService tracingService;

    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;

    public AbstractConstraintAnalyzer(final AnalysisCommon analysisCommon, final IResourceService resourceService,
            final IStrategoRuntimeService runtimeService, final IStrategoCommon strategoCommon,
            final ITermFactoryService termFactoryService, final ISpoofaxTracingService tracingService) {
        this.analysisCommon = analysisCommon;
        this.resourceService = resourceService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.tracingService = tracingService;
        this.termFactory = termFactoryService.getGeneric();
        this.strategoTerms = new StrategoTerms(termFactory);
    }

    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext, IProgress progress,
            ICancel cancel) throws AnalysisException {
        if(!input.valid()) {
            final String message = logger.format("Parse input for {} is invalid, cannot analyze", input.source());
            throw new AnalysisException(genericContext, message);
        }
        final ISpoofaxAnalyzeResults results =
                analyzeAll(Iterables2.singleton(input), genericContext, progress, cancel);
        if(results.results().isEmpty()) {
            throw new AnalysisException(genericContext, "Analysis failed.");
        }
        return new SpoofaxAnalyzeResult(Iterables.getOnlyElement(results.results()), results.updates(),
                results.context());
    }

    @SuppressWarnings("unchecked") @Override public ISpoofaxAnalyzeResults
            analyzeAll(Iterable<ISpoofaxParseUnit> inputs, IContext genericContext, IProgress progress, ICancel cancel)
                    throws AnalysisException {
        C context;
        try {
            context = (C) genericContext;
        } catch(ClassCastException ex) {
            throw new AnalysisException(genericContext, "Scope graph context required for constraint analysis.", ex);
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

        Map<String, ISpoofaxParseUnit> changed = Maps.newHashMap();
        Set<String> removed = Sets.newHashSet();
        for(ISpoofaxParseUnit input : inputs) {
            final String source;
            if(input.detached()) {
                source = "detached-" + UUID.randomUUID().toString();
            } else {
                source = input.source().getName().getURI();
            }
            if(!input.valid() || isEmptyAST(input.ast())) {
                removed.add(source);
            } else {
                changed.put(source, input);
            }
        }
        return analyzeAll(changed, removed, context, runtime, facet.strategyName, progress, cancel);
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return Tools.isTermTuple(ast) && ast.getSubtermCount() == 0;
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed, Set<String> removed,
            C context, HybridInterpreter runtime, String strategy, IProgress progress, ICancel cancel)
            throws AnalysisException;

    protected Optional<ITerm> doAction(String strategy, ITerm action, ISpoofaxScopeGraphContext<?> context,
            HybridInterpreter runtime) throws AnalysisException {
        try {
            return Optional.ofNullable(strategoCommon.invoke(runtime, strategoTerms.toStratego(action), strategy))
                    .map(strategoTerms::fromStratego);
        } catch(MetaborgException ex) {
            final String message = "Analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }

    protected Optional<ITerm> doCustomAction(String strategy, ITerm action, ISpoofaxScopeGraphContext<?> context,
            HybridInterpreter runtime) {
        try {
            return doAction(strategy, action, context, runtime);
        } catch(Exception ex) {
            logger.warn("Custom analysis step failed.", ex);
            return Optional.empty();
        }
    }


    protected Collection<IMessage> merge(Collection<IMessage> m1, Collection<IMessage> m2) {
        List<IMessage> m = Lists.newArrayList();
        m.addAll(m1);
        m.addAll(m2);
        return m;
    }

    protected Multimap<String, IMessage> messagesByFile(Collection<IMessage> messages) {
        Multimap<String, IMessage> fmessages = HashMultimap.create();
        for(IMessage message : messages) {
            fmessages.put(message.source().getName().getURI(), message);
        }
        return fmessages;
    }

    protected Set<IMessage> messages(Collection<IMessageInfo> constraintMessages,
            Collection<IMessageInfo> unsolvedMessages, Optional<Collection<EditorMessage>> customMessages,
            IUnifier unifier, MessageSeverity severity) {
        Set<IMessage> messages = Sets.newHashSet();
        constraintMessages.stream().map(m -> message(m, unifier, severity)).forEach(m -> m.ifPresent(messages::add));
        unsolvedMessages.stream().map(m -> message(m, unifier, severity)).forEach(m -> m.ifPresent(messages::add));
        customMessages.ifPresent(
                cms -> cms.stream().map(m -> message(m, unifier, severity)).forEach(m -> m.ifPresent(messages::add)));
        return messages;
    }

    private Optional<IMessage> message(EditorMessage message, IUnifier unifier, MessageSeverity severity) {
        return message(message.getOrigin(), message.getMessage(), severity);
    }

    private Optional<IMessage> message(IMessageInfo message, IUnifier unifier, MessageSeverity severity) {
        return message(message.getOriginTerm(), message.getContent().apply(unifier::find).toString(null), severity);
    }

    private Optional<IMessage> message(ITerm originatingTerm, String message, MessageSeverity severity) {
        Optional<TermOrigin> maybeOrigin = TermOrigin.get(originatingTerm);
        if(maybeOrigin.isPresent()) {
            TermOrigin origin = maybeOrigin.get();
            SourceRegion region = new SourceRegion(origin.getStartOffset(), origin.getStartLine(),
                    origin.getStartColumn(), origin.getEndOffset(), origin.getEndLine(), origin.getEndColumn());
            FileObject resource = resourceService.resolve(origin.getResource());
            return Optional.of(MessageFactory.newAnalysisMessage(resource, region, message, severity, null));
        } else {
            logger.warn("Ignoring location-less {}: {}", severity, message);
            return Optional.empty();
        }
    }

}