package org.metaborg.spoofax.core.analysis.constraint;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
import org.metaborg.meta.nabl2.spoofax.TermSimplifier;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.stratego.TermOrigin;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.unification.IUnifier;
import org.metaborg.meta.nabl2.util.collections.HashRelation3;
import org.metaborg.meta.nabl2.util.collections.IRelation3;
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
import org.metaborg.util.resource.ResourceUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

abstract class AbstractConstraintAnalyzer<C extends ISpoofaxScopeGraphContext<?>> implements ISpoofaxAnalyzer {

    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    private static final String PP_STRATEGY = "pp-NaBL2-objlangterm";

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
                source = resource(input.source(), context);
            }
            if(!input.valid() || isEmptyAST(input.ast())) {
                removed.add(source);
            } else {
                changed.put(source, input);
            }
        }
        return analyzeAll(changed, removed, context, runtime, facet.strategyName, progress, cancel);
    }

    protected String resource(FileObject resource, C context) {
        return ResourceUtils.relativeName(resource.getName(), context.location().getName(), false);
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return Tools.isTermTuple(ast) && ast.getSubtermCount() == 0;
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed, Set<String> removed,
            C context, HybridInterpreter runtime, String strategy, IProgress progress, ICancel cancel)
            throws AnalysisException;

    protected Optional<ITerm> doAction(String strategy, ITerm action, C context, HybridInterpreter runtime)
            throws AnalysisException {
        try {
            return Optional.ofNullable(strategoCommon.invoke(runtime, strategoTerms.toStratego(action), strategy))
                    .map(strategoTerms::fromStratego);
        } catch(MetaborgException ex) {
            final String message = "Analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }

    protected Optional<ITerm> doCustomAction(String strategy, ITerm action, C context, HybridInterpreter runtime)
            throws AnalysisException {
        try {
            return doAction(strategy, action, context, runtime);
        } catch(Exception ex) {
            final String message = "Custom analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }


    protected IRelation3.Mutable<FileObject, MessageSeverity, IMessage> messagesByFile(Iterable<IMessage> messages) {
        IRelation3.Mutable<FileObject, MessageSeverity, IMessage> fmessages = HashRelation3.create();
        for(IMessage message : messages) {
            fmessages.put(message.source(), message.severity(), message);
        }
        return fmessages;
    }

    protected Set<IMessage> messages(Iterable<IMessageInfo> messages, IUnifier unifier, C context,
            FileObject defaultLocation) {
        return Iterables2.stream(messages).map(m -> message(m, unifier, context, defaultLocation))
                .collect(Collectors.toSet());
    }

    private IMessage message(IMessageInfo message, IUnifier unifier, C context, FileObject defaultLocation) {
        final MessageSeverity severity;
        switch(message.getKind()) {
            default:
            case ERROR:
                severity = MessageSeverity.ERROR;
                break;
            case WARNING:
                severity = MessageSeverity.WARNING;
                break;
            case NOTE:
                severity = MessageSeverity.NOTE;
                break;
        }
        return message(message.getOriginTerm(), message, severity, unifier, context, defaultLocation);
    }

    private IMessage message(ITerm originatingTerm, IMessageInfo messageInfo, MessageSeverity severity,
            IUnifier unifier, C context, FileObject defaultLocation) {
        Optional<TermOrigin> maybeOrigin = TermOrigin.get(originatingTerm);
        if(maybeOrigin.isPresent()) {
            TermOrigin origin = maybeOrigin.get();
            SourceRegion region = new SourceRegion(origin.getStartOffset(), origin.getStartLine(),
                    origin.getStartColumn(), origin.getEndOffset(), origin.getEndLine(), origin.getEndColumn());
            FileObject resource = resourceService.resolve(context.location(), origin.getResource());
            String message = messageInfo.getContent().apply(unifier::find)
                    .toString(prettyprint(context, resource(resource, context)));
            return MessageFactory.newAnalysisMessage(resource, region, message, severity, null);
        } else {
            String message = messageInfo.getContent().apply(unifier::find).toString(prettyprint(context, null));
            return MessageFactory.newAnalysisMessageAtTop(defaultLocation, message, severity, null);
        }
    }

    protected Function<ITerm, String> prettyprint(C context, @Nullable String resource) {
        return term -> {
            final ITerm simpleTerm = TermSimplifier.focus(resource, term);
            final IStrategoTerm sterm = strategoTerms.toStratego(simpleTerm);
            String text;
            try {
                text = Optional.ofNullable(strategoCommon.invoke(context.language(), context, sterm, PP_STRATEGY))
                        .map(Tools::asJavaString).orElseGet(() -> simpleTerm.toString());
            } catch(MetaborgException ex) {
                logger.warn("Pretty-printing failed on {}, using simple term representation.", ex, simpleTerm);
                text = simpleTerm.toString();
            }
            return text;
        };
    }

}
