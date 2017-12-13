package org.metaborg.spoofax.core.analysis.constraint;

import static meta.flowspec.java.Path.TRANSFER_FUNCTIONS_FILE;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.SourceRegion;
import org.metaborg.meta.nabl2.constraints.messages.IMessageInfo;
import org.metaborg.meta.nabl2.controlflow.terms.CFGNode;
import org.metaborg.meta.nabl2.solver.ISolution;
import org.metaborg.meta.nabl2.solver.ImmutableSolution;
import org.metaborg.meta.nabl2.solver.messages.IMessages;
import org.metaborg.meta.nabl2.solver.solvers.CallExternal;
import org.metaborg.meta.nabl2.spoofax.TermSimplifier;
import org.metaborg.meta.nabl2.stratego.ConstraintTerms;
import org.metaborg.meta.nabl2.stratego.ImmutableTermIndex;
import org.metaborg.meta.nabl2.stratego.StrategoTerms;
import org.metaborg.meta.nabl2.stratego.TermIndex;
import org.metaborg.meta.nabl2.stratego.TermOrigin;
import org.metaborg.meta.nabl2.terms.ITerm;
import org.metaborg.meta.nabl2.terms.generic.TB;
import org.metaborg.meta.nabl2.unification.IUnifier;
import org.metaborg.meta.nabl2.util.collections.IProperties;
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
import org.spoofax.terms.ParseError;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import meta.flowspec.nabl2.controlflow.IControlFlowGraph;
import meta.flowspec.nabl2.util.tuples.Tuple2;

abstract class AbstractConstraintAnalyzer<C extends ISpoofaxScopeGraphContext<?>>
        implements ISpoofaxAnalyzer, ILanguageCache {

    private static final ILogger logger = LoggerUtils.logger(AbstractConstraintAnalyzer.class);

    private static final String PP_STRATEGY = "pp-NaBL2-objlangterm";

    protected final AnalysisCommon analysisCommon;
    protected final IResourceService resourceService;
    protected final IStrategoRuntimeService runtimeService;
    protected final IStrategoCommon strategoCommon;
    protected final ISpoofaxTracingService tracingService;

    protected final ITermFactory termFactory;
    protected final StrategoTerms strategoTerms;
    protected final Map<ILanguageComponent, IStrategoTerm> flowSpecTransferFunctionCache = new HashMap<>();

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

    @Override
    public void invalidateCache(ILanguageComponent component) {
        logger.debug("Removing cached flowspec transfer functions for {}", component);
        flowSpecTransferFunctionCache.remove(component);
    }

    @Override
    public void invalidateCache(ILanguageImpl impl) {
        logger.debug("Removing cached flowspec transfer functions for {}", impl);
        for (ILanguageComponent component : impl.components()) {
            flowSpecTransferFunctionCache.remove(component);
        }
    }

    protected String resource(FileObject resource, C context) {
        return ResourceUtils.relativeName(resource.getName(), context.location().getName(), true);
    }

    protected FileObject resource(String resource, C context) {
        return resourceService.resolve(context.location(), resource);
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return Tools.isTermTuple(ast) && ast.getSubtermCount() == 0;
    }

    protected IStrategoTerm getFlowSpecTransferFunctions(ILanguageComponent component) {
        IStrategoTerm transferFunctions = flowSpecTransferFunctionCache.get(component);
        if (transferFunctions != null) {
            return transferFunctions;
        }

        FileObject tfs = resourceService.resolve(component.location(), TRANSFER_FUNCTIONS_FILE);
        try {
            transferFunctions = termFactory
                    .parseFromString(IOUtils.toString(tfs.getContent().getInputStream(), StandardCharsets.UTF_8));
        } catch (ParseError | IOException e) {
            logger.error("Could not read transfer functions file for {}", component);
            throw new MetaborgRuntimeException("Could not read transfer functions file", e);
        }
        flowSpecTransferFunctionCache.put(component, transferFunctions);
        return transferFunctions;
    }

    protected List<IStrategoTerm> getFlowSpecTransferFunctions(ILanguageImpl impl) {
        List<IStrategoTerm> result = new ArrayList<>();
        for (ILanguageComponent comp : impl.components()) {
            result.add(getFlowSpecTransferFunctions(comp));
        }
        return result;
    }

    protected abstract ISpoofaxAnalyzeResults analyzeAll(Map<String, ISpoofaxParseUnit> changed, Set<String> removed,
            C context, HybridInterpreter runtime, String strategy, IProgress progress, ICancel cancel)
            throws AnalysisException;

    // this function does not handle specialization and explication, which is left to the analysis input and output
    // matchers and builders
    protected Optional<ITerm> doAction(String strategy, ITerm action, C context, HybridInterpreter runtime)
            throws AnalysisException {
        try {
            return Optional
                    .ofNullable(strategoCommon.invoke(runtime,
                            strategoTerms.toStratego(ConstraintTerms.explicate(action)), strategy))
                    .map(strategoTerms::fromStratego);
        } catch(MetaborgException ex) {
            final String message = "Analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }

    protected Optional<ITerm> doCustomAction(String strategy, ITerm action, C context, HybridInterpreter runtime)
            throws AnalysisException {
        try {
            return Optional
                    .ofNullable(strategoCommon.invoke(runtime,
                            strategoTerms.toStratego(ConstraintTerms.explicate(action)), strategy))
                    .map(strategoTerms::fromStratego);
        } catch(Exception ex) {
            final String message = "Custom analysis failed.\n" + ex.getMessage();
            throw new AnalysisException(context, message, ex);
        }
    }

    protected CallExternal callExternal(HybridInterpreter runtime) {
        return (name, args) -> {
            final IStrategoTerm[] sargs = Iterables2.stream(args).map(strategoTerms::toStratego)
                    .collect(Collectors.toList()).toArray(new IStrategoTerm[0]);
            final IStrategoTerm sarg = sargs.length == 1 ? sargs[0] : termFactory.makeTuple(sargs);
            try {
                final IStrategoTerm sresult = strategoCommon.invoke(runtime, sarg, name);
                return Optional.ofNullable(sresult).map(strategoTerms::fromStratego).map(ConstraintTerms::specialize);
            } catch(Exception ex) {
                logger.warn("External call to '{}' failed.", name);
                return Optional.empty();
            }
        };
    }


    protected void messagesByFile(Iterable<IMessage> messages,
            IRelation3.Transient<String, MessageSeverity, IMessage> fmessages, C context) {
        for(IMessage message : messages) {
            fmessages.put(resource(message.source(), context), message.severity(), message);
        }
    }

    protected void countMessages(IMessages messages, AtomicInteger numErrors, AtomicInteger numWarnings,
            AtomicInteger numNotes) {
        numErrors.addAndGet(messages.getErrors().size());
        numWarnings.addAndGet(messages.getWarnings().size());
        numNotes.addAndGet(messages.getNotes().size());
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
            final ITerm simpleTerm = ConstraintTerms.explicate(TermSimplifier.focus(resource, term));
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

    protected void flowspecDemoOutput(C context, final IControlFlowGraph<CFGNode> controlFlowGraph) {
        logger.debug("Outputting FlowSpec demo output file");
        FileObject file = this.resource("target/flowspec-out.aterm", context);
        try (PrintWriter out = new PrintWriter(file.getContent().getOutputStream())) {
            out.println("// CFG");
            for (Map.Entry<CFGNode, CFGNode> e : controlFlowGraph.getDirectEdges().entrySet()) {
                out.println("(" + e.getKey() + ", " + e.getValue() + ")");
            }
            out.println("// Properties");
            for (Map.Entry<Tuple2<CFGNode, String>, ITerm> e : controlFlowGraph.getProperties().entrySet()) {
                out.println(e.getKey()._2() + "(" + e.getKey()._1() + ") = " + e.getValue().toString());
            }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    protected static ISolution flowspecCopyProperties(ISolution solution) {
        logger.debug("Copying FlowSpec properties to NaBL2 ast properties in solution");
        IProperties.Transient<TermIndex, ITerm, ITerm> astProperties = solution.astProperties().melt();
        IControlFlowGraph<CFGNode> controlFlowGraph = solution.controlFlowGraph();

        for (Map.Entry<Tuple2<CFGNode, String>, ITerm> property : controlFlowGraph.getProperties().entrySet()) {
            CFGNode node = property.getKey()._1();
            String propName = property.getKey()._2();
            ITerm value = property.getValue();

            TermIndex ti = TermIndex.get(node).orElse(ImmutableTermIndex.of(node.getResource(), 0));

            astProperties.putValue(ti, TB.newAppl("Property", TB.newString(propName)), value);
        }

        return ImmutableSolution.of(solution.config(), astProperties.freeze(), solution.scopeGraph(),
                solution.nameResolution(), solution.declProperties(), solution.relations(), solution.unifier(),
                solution.symbolic(), solution.controlFlowGraph(), solution.messages(), solution.constraints());
    }

}
