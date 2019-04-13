package org.metaborg.spoofax.core.analysis.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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

    protected abstract boolean multifile();

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
            if(input.detached() || input.source() == null) {
                logger.warn("Ignoring detached units");
            }
            final String source = context.resourceKey(input.source());
            if(input.valid() && input.success() && !isEmptyAST(input.ast())) {
                changed.put(source, input);
            } else {
                removed.put(source, input);
            }
        }

        return doAnalysis(changed, removed, context, runtime, facet.strategyName, progress, cancel);
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return Tools.isTermTuple(ast) && ast.getSubtermCount() == 0;
    }

    private ISpoofaxAnalyzeResults doAnalysis(Map<String, ISpoofaxParseUnit> changed,
            Map<String, ISpoofaxParseUnit> removed, IConstraintContext context, HybridInterpreter runtime,
            String strategy, IProgress progress, ICancel cancel) throws AnalysisException {

        /*******************************************************************
         * 1. Compute changeset, and remove invalidated units from context *
         *******************************************************************/

        final List<IStrategoTerm> changes = new ArrayList<>();
        final Map<String, Expect> expects = new HashMap<>();

        // project entry
        final IStrategoTerm projectChange;
        if(multifile()) {
            final String resource = context.resourceKey(context.root());
            final IStrategoTerm ast = termFactory.makeTuple();
            final IStrategoTerm change;
            final Expect expect;
            if(context.contains(resource)) {
                final IStrategoTerm analysis = context.get(resource);
                change = build("Cached", analysis);
                expect = new Update(resource, context);
                context.remove(resource);
            } else {
                change = build("Added", ast);
                expect = new Project(resource, context);
            }
            expects.put(resource, expect);
            projectChange = termFactory.makeTuple(termFactory.makeString(resource), change);
        } else {
            projectChange = null;
        }

        // removed files
        for(Map.Entry<String, ISpoofaxParseUnit> entry : removed.entrySet()) {
            final String resource = entry.getKey();
            if(context.contains(entry.getKey())) {
                final IStrategoTerm analysis = context.get(resource);
                changes.add(termFactory.makeTuple(termFactory.makeString(resource), build("Removed", analysis)));
                context.remove(resource);
            }
        }

        // added and changed files
        for(Map.Entry<String, ISpoofaxParseUnit> entry : changed.entrySet()) {
            final String resource = entry.getKey();
            final ISpoofaxParseUnit input = entry.getValue();
            final IStrategoTerm ast = input.ast();
            final IStrategoTerm change;
            if(context.contains(resource)) {
                final IStrategoTerm analysis = context.get(resource);
                change = build("Changed", ast, analysis);
                context.remove(resource);
            } else {
                change = build("Added", ast);
            }
            expects.put(resource, new Full(resource, input, context));
            changes.add(termFactory.makeTuple(termFactory.makeString(resource), change));
        }

        // cached files
        for(Map.Entry<String, IStrategoTerm> entry : context.entrySet()) {
            final String resource = entry.getKey();
            final IStrategoTerm analysis = entry.getValue();
            if(!changed.containsKey(resource)) {
                final IStrategoTerm change = build("Cached", analysis);
                expects.put(resource, new Update(resource, context));
                changes.add(termFactory.makeTuple(termFactory.makeString(resource), change));
            }
        }

        /***************************************
         * 2. Call analysis, and parse results *
         ***************************************/

        final Map<String, IStrategoTerm> results = new HashMap<>();
        final IStrategoTerm action;
        if(multifile()) {
            action = build("AnalyzeMulti", projectChange, termFactory.makeList(changes));
        } else {
            action = build("AnalyzeSingle", termFactory.makeList(changes));
        }
        final IStrategoTerm allResultsTerm;
        try {
            allResultsTerm = strategoCommon.invoke(runtime, action, strategy);
        } catch(MetaborgException ex) {
            throw new AnalysisException(context, ex);
        }
        if(allResultsTerm == null) {
            throw new AnalysisException(context, "Analysis strategy failed");
        }
        final List<IStrategoTerm> allResultTerms;
        if((allResultTerms = match(allResultsTerm, "AnalysisResult", 1)) == null) {
            throw new AnalysisException(context, "Invalid analysis result, got " + allResultsTerm);
        }
        final IStrategoTerm resultsTerm = allResultTerms.get(0);
        if(!Tools.isTermList(resultsTerm)) {
            throw new AnalysisException(context, "Expected list of results, got " + resultsTerm);
        }
        for(IStrategoTerm entry : resultsTerm.getAllSubterms()) {
            if(!Tools.isTermTuple(entry) || entry.getSubtermCount() != 2) {
                throw new AnalysisException(context, "Expected tuple result, got " + entry);
            }
            final IStrategoTerm resourceTerm = entry.getSubterm(0);
            final IStrategoTerm resultTerm = entry.getSubterm(1);
            if(!Tools.isTermString(resourceTerm)) {
                throw new AnalysisException(context, "Expected resource string as first component, got " + resourceTerm);
            }
            final String resource = Tools.asJavaString(resourceTerm);
            results.put(resource, resultTerm);
        }

        /*******************************
         * 3. Process analysis results *
         *******************************/

        // call expects with result
        for(Map.Entry<String, IStrategoTerm> entry : results.entrySet()) {
            final String resource = entry.getKey();
            final IStrategoTerm result = entry.getValue();
            if(expects.containsKey(resource)) {
                expects.get(resource).accept(result);
            } else {
                logger.warn("Got result for invalid file.");
            }
        }

        // check coverage
        for(Map.Entry<String, ISpoofaxParseUnit> entry : changed.entrySet()) {
            final String resource = entry.getKey();
            if(!results.containsKey(resource)) {
                expects.get(resource).failMessage("Missing analysis result");
            }
        }

        /**************************************
         * 4. Globally collect error messages *
         **************************************/

        final Multimap<FileObject, IMessage> messages = HashMultimap.create();
        for(Map.Entry<String, Expect> entry : expects.entrySet()) {
            final Expect expect = entry.getValue();
            messages.putAll(expect.messages);
        }

        /************************************
         * 5. Create Spoofax analysis units *
         ************************************/

        final Set<ISpoofaxAnalyzeUnit> fullResults = Sets.newHashSet();
        final Set<ISpoofaxAnalyzeUnitUpdate> updateResults = Sets.newHashSet();
        for(Expect expect : expects.values()) {
            expect.result(messages.get(expect.resource()), fullResults, updateResults);
        }
        return new SpoofaxAnalyzeResults(fullResults, updateResults, context, null);
    }

    private abstract class Expect {

        protected final String resource;
        protected final IConstraintContext context;
        protected final Multimap<FileObject, IMessage> messages;

        protected Expect(String resource, IConstraintContext context) {
            this.resource = resource;
            this.context = context;
            this.messages = HashMultimap.create();
        }

        protected FileObject resource() {
            return context.keyResource(resource);
        }

        protected void resultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes) {
            if(multifile()) {
                messages.putAll(analysisCommon.messages(MessageSeverity.ERROR, errors));
                messages.putAll(analysisCommon.messages(MessageSeverity.WARNING, warnings));
                messages.putAll(analysisCommon.messages(MessageSeverity.NOTE, notes));
            } else {
                messages.putAll(resource(), analysisCommon.messages(resource(), MessageSeverity.ERROR, errors));
                messages.putAll(resource(), analysisCommon.messages(resource(), MessageSeverity.WARNING, warnings));
                messages.putAll(resource(), analysisCommon.messages(resource(), MessageSeverity.NOTE, notes));

            }
        }

        protected void failMessage(String message) {
            messages.put(resource(), MessageFactory.newAnalysisErrorAtTop(resource(), message, null));
        }

        abstract void accept(IStrategoTerm result);

        abstract void result(Collection<IMessage> messages, Collection<ISpoofaxAnalyzeUnit> fullResults,
                Collection<ISpoofaxAnalyzeUnitUpdate> updateResults);

    }

    private class Full extends Expect {

        // 1. initialized by constructor
        private ISpoofaxParseUnit input;
        // 2. initialized by accept
        private IStrategoTerm ast;
        private IStrategoTerm analysis;

        public Full(String resource, ISpoofaxParseUnit input, IConstraintContext context) {
            super(resource, context);
            this.input = input;
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Full", 5)) != null) {
                ast = results.get(0);
                analysis = results.get(1);
                resultMessages(results.get(2), results.get(3), results.get(4));
                if(!input.detached()) {
                    context.put(resource, analysis);
                }
            } else if(match(result, "Failed", 0) != null) {
                ast = null;
                analysis = null;
                failMessage("Analysis failed");
                if(!input.detached()) {
                    context.remove(resource);
                }
            } else {
                failMessage("Analysis returned incorrect result");
            }
        }

        @Override public void result(Collection<IMessage> messages, Collection<ISpoofaxAnalyzeUnit> fullResults,
                Collection<ISpoofaxAnalyzeUnitUpdate> updateResults) {
            fullResults.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(ast != null, success(messages), true, ast, messages, -1), context));
        }

    }

    private class Update extends Expect {

        // 2. initialized by accept
        private IStrategoTerm analysis;

        private Update(String resource, IConstraintContext context) {
            super(resource, context);
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Update", 4)) != null) {
                analysis = results.get(0);
                resultMessages(results.get(1), results.get(2), results.get(3));
                context.put(resource, analysis);
            } else if(match(result, "Failed", 0) != null) {
                analysis = null;
                failMessage("Analysis failed");
                context.remove(resource);
            } else {
                failMessage("Analysis returned incorrect result");
            }
        }

        @Override public void result(Collection<IMessage> messages, Collection<ISpoofaxAnalyzeUnit> fullResults,
                Collection<ISpoofaxAnalyzeUnitUpdate> updateResults) {
            updateResults.add(unitService.analyzeUnitUpdate(resource(), new AnalyzeUpdateData(messages), context));
        }

    }

    private class Project extends Expect {

        // 2. initialized by accept
        private IStrategoTerm analysis;

        public Project(String resource, IConstraintContext context) {
            super(resource, context);
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Full", 5)) != null) {
                analysis = results.get(1);
                resultMessages(results.get(2), results.get(3), results.get(4));
                context.put(resource, analysis);
            } else if(match(result, "Failed", 0) != null) {
                analysis = null;
                failMessage("Analysis failed");
                context.remove(resource);
            } else {
                failMessage("Analysis returned incorrect result");
            }
        }

        @Override public void result(Collection<IMessage> messages, Collection<ISpoofaxAnalyzeUnit> fullResults,
                Collection<ISpoofaxAnalyzeUnitUpdate> updateResults) {
            updateResults.add(unitService.analyzeUnitUpdate(resource(), new AnalyzeUpdateData(messages), context));
        }

    }

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
