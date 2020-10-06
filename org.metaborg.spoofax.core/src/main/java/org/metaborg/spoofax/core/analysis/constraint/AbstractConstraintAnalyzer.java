package org.metaborg.spoofax.core.analysis.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
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
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.unit.AnalyzeContrib;
import org.metaborg.spoofax.core.unit.AnalyzeUpdateData;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.Ref;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.metaborg.util.time.Timer;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.HybridInterpreter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mb.flowspec.terms.B;
import mb.nabl2.terms.stratego.StrategoTermIndices;
import mb.nabl2.terms.stratego.TermIndex;
import mb.nabl2.terms.stratego.TermOrigin;

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
            final ITermFactory termFactory, final ISpoofaxTracingService tracingService,
            final ISpoofaxUnitService unitService) {
        this.analysisCommon = analysisCommon;
        this.resourceService = resourceService;
        this.runtimeService = runtimeService;
        this.strategoCommon = strategoCommon;
        this.tracingService = tracingService;
        this.unitService = unitService;
        this.termFactory = termFactory;
    }

    protected abstract boolean multifile();

    @Override public ISpoofaxAnalyzeResult analyze(ISpoofaxParseUnit input, IContext genericContext, IProgress progress,
            ICancel cancel) throws AnalysisException {
        final ISpoofaxAnalyzeResults results =
                analyzeAll(Iterables2.singleton(input), genericContext, progress, cancel);
        if(results.results().isEmpty() && results.updates().isEmpty()) {
            throw new AnalysisException(genericContext, "Analysis failed, no result was returned.");
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
            runtime = runtimeService.runtime(facetContribution.contributor, context);
        } catch(MetaborgException e) {
            throw new AnalysisException(context, "Failed to get Stratego runtime", e);
        }

        final Map<String, ISpoofaxParseUnit> changed = Maps.newHashMap();
        final Map<String, ISpoofaxAnalyzeUnit> removed = Maps.newHashMap();
        final Map<String, ISpoofaxAnalyzeUnit> invalid = Maps.newHashMap();
        for(ISpoofaxParseUnit input : inputs) {
            if(input.detached() || input.source() == null) {
                logger.warn("Ignoring detached units");
                continue;
            }
            final String source = context.resourceKey(input.source());
            if(!input.valid() || !input.success()) {
                invalid.put(source, unitService.emptyAnalyzeUnit(input, context));
                continue;
            }

            if(!isEmptyAST(input.ast())) {
                changed.put(source, input);
            } else {
                removed.put(source, unitService.emptyAnalyzeUnit(input, context));
            }
        }

        final Timer timer = new Timer(true);
        try {
            return doAnalysis(changed, removed, invalid, context, runtime, facet.strategyName, progress, cancel);
        } finally {
            logger.info("Analysis finished in {} s", timer.stop() / 1_000_000_000d);
        }
    }

    private boolean isEmptyAST(IStrategoTerm ast) {
        return TermUtils.isTuple(ast, 0);
    }

    private ISpoofaxAnalyzeResults doAnalysis(Map<String, ISpoofaxParseUnit> changed,
            Map<String, ISpoofaxAnalyzeUnit> removed, Map<String, ISpoofaxAnalyzeUnit> invalid,
            IConstraintContext context, HybridInterpreter runtime, String strategy, IProgress progress, ICancel cancel)
            throws AnalysisException {

        /*******************************************************************
         * 1. Compute changeset, and remove invalidated units from context *
         *******************************************************************/

        final Ref<IStrategoTerm> projectChange = new Ref<>();
        final List<IStrategoTerm> changes = new ArrayList<>();
        final Map<String, Expect> expects = new HashMap<>();

        final boolean realChange = computeChanges(context, changed, removed, projectChange, changes, expects);

        /***************************************
         * 2. Call analysis, and parse results *
         ***************************************/

        final Map<String, IStrategoTerm> results = new HashMap<>();

        if(realChange) {

            callAnalysis(context, changed, projectChange.get(), changes, expects, runtime, strategy, cancel, progress,
                    results);

        }

        /**************************************************
         * 3. Process analysis results & collect messages *
         **************************************************/

        final ListMultimap<FileName, IMessage> messages = ArrayListMultimap.create();

        processResults(changed, expects, results, messages);

        /************************************
         * 4. Create Spoofax analysis units *
         ************************************/

        final Set<ISpoofaxAnalyzeUnit> fullResults = Sets.newHashSet();
        final Set<ISpoofaxAnalyzeUnitUpdate> updateResults = Sets.newHashSet();
        for(Expect expect : expects.values()) {
            Collection<IMessage> fileMessages = messages.get(expect.resource().getName());
            expect.result(fileMessages, fullResults, updateResults);
        }
        fullResults.addAll(removed.values());
        fullResults.addAll(invalid.values());
        return new SpoofaxAnalyzeResults(fullResults, updateResults, context, null);

    }

    private boolean computeChanges(IConstraintContext context, Map<String, ISpoofaxParseUnit> changed,
            Map<String, ISpoofaxAnalyzeUnit> removed, Ref<IStrategoTerm> projectChange,
            final List<IStrategoTerm> changes, final Map<String, Expect> expects) {
        boolean realChange = false;

        // project entry
        if(multifile()) {
            final String resource = context.resourceKey(context.location());
            final IStrategoTerm projectAst = projectAST(resource);
            final IStrategoTerm change;
            final Expect expect;
            if(context.contains(resource)) {
                final IConstraintContext.Entry ctxEntry = context.get(resource);
                change = build("Cached", ctxEntry.analysis());
                expect = new Update(resource, projectAst.hashCode(), projectAst, ctxEntry.analysis(), ctxEntry.errors(),
                        ctxEntry.warnings(), ctxEntry.notes(), ctxEntry.exceptions(), context);
                context.remove(resource);
            } else {
                change = build("Added", projectAst);
                expect = new ProjectFull(resource, projectAst.hashCode(), projectAst, context);
                realChange = true;
            }
            expects.put(resource, expect);
            projectChange.set(termFactory.makeTuple(termFactory.makeString(resource), change));
        } else {
            projectChange.set(null);
        }

        // removed files
        for(Map.Entry<String, ISpoofaxAnalyzeUnit> entry : removed.entrySet()) {
            final String resource = entry.getKey();
            if(context.contains(entry.getKey())) {
                final IConstraintContext.Entry ctxEntry = context.get(resource);
                changes.add(
                        termFactory.makeTuple(termFactory.makeString(resource), build("Removed", ctxEntry.analysis())));
                context.remove(resource);
                realChange = true;
            }
        }

        // added and changed files
        for(Map.Entry<String, ISpoofaxParseUnit> entry : changed.entrySet()) {
            final String resource = entry.getKey();
            final ISpoofaxParseUnit input = entry.getValue();
            final IStrategoTerm parseAst = input.ast();
            final IStrategoTerm change;
            final Expect expect;
            if(context.contains(resource)) {
                final IConstraintContext.Entry ctxEntry = context.get(resource);
                final IStrategoTerm analyzedAst = ctxEntry.analyzedAst();
                if(context.hasChanged(resource, parseAst.hashCode()) || analyzedAst == null) {
                    change = build("Changed", parseAst, ctxEntry.analysis());
                    expect = new Full(resource, parseAst.hashCode(), input, context);
                    realChange = true;
                } else {
                    change = build("Cached", ctxEntry.analysis());
                    expect = new UpdateFull(resource, parseAst.hashCode(), analyzedAst, ctxEntry.analysis(),
                            ctxEntry.errors(), ctxEntry.warnings(), ctxEntry.notes(), ctxEntry.exceptions(), input,
                            context);
                }
            } else {
                change = build("Added", parseAst);
                expect = new Full(resource, parseAst.hashCode(), input, context);
                realChange = true;
            }
            context.remove(resource);
            expects.put(resource, expect);
            changes.add(termFactory.makeTuple(termFactory.makeString(resource), change));
        }

        // cached files
        if(multifile()) {
            for(Map.Entry<String, IConstraintContext.Entry> entry : context.entrySet()) {
                final String resource = entry.getKey();
                final IConstraintContext.Entry ctxEntry = entry.getValue();
                final IStrategoTerm analyzedAst = ctxEntry.analyzedAst();
                final IStrategoTerm analysis = ctxEntry.analysis();
                if(!changed.containsKey(resource)) {
                    final IStrategoTerm change = build("Cached", analysis);
                    expects.put(resource, new Update(resource, entry.getValue().parseHash(), analyzedAst, analysis,
                            ctxEntry.errors(), ctxEntry.warnings(), ctxEntry.notes(), ctxEntry.exceptions(), context));
                    changes.add(termFactory.makeTuple(termFactory.makeString(resource), change));
                }
            }
        }

        return realChange;
    }

    private void callAnalysis(IConstraintContext context, Map<String, ISpoofaxParseUnit> changed,
            final IStrategoTerm projectChange, final List<IStrategoTerm> changes, Map<String, Expect> expects,
            HybridInterpreter runtime, String strategy, ICancel cancel, IProgress progress,
            final Map<String, IStrategoTerm> results) throws AnalysisException {

        final IStrategoTerm action;
        if(multifile()) {
            action = build("AnalyzeMulti", projectChange, termFactory.makeList(changes), B.blob(progress),
                    B.blob(cancel));
        } else {
            action = build("AnalyzeSingle", termFactory.makeList(changes), B.blob(progress), B.blob(cancel));
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
        if(!TermUtils.isList(resultsTerm)) {
            throw new AnalysisException(context, "Expected list of results, got " + resultsTerm);
        }
        for(IStrategoTerm entry : resultsTerm.getAllSubterms()) {
            if(!TermUtils.isTuple(entry, 2)) {
                throw new AnalysisException(context, "Expected tuple result, got " + entry);
            }
            final IStrategoTerm resourceTerm = entry.getSubterm(0);
            final IStrategoTerm resultTerm = entry.getSubterm(1);
            if(!TermUtils.isString(resourceTerm)) {
                throw new AnalysisException(context,
                        "Expected resource string as first component, got " + resourceTerm);
            }
            final String resource = TermUtils.toJavaString(resourceTerm);
            results.put(resource, resultTerm);
        }

        // check coverage
        for(Map.Entry<String, ISpoofaxParseUnit> entry : changed.entrySet()) {
            final String resource = entry.getKey();
            if(!results.containsKey(resource)) {
                expects.get(resource).failMessage("Missing analysis result");
            }
        }

    }

    private void processResults(Map<String, ISpoofaxParseUnit> changed, final Map<String, Expect> expects,
            final Map<String, IStrategoTerm> results, ListMultimap<FileName, IMessage> messages) {

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

        // collect messages
        for(Map.Entry<String, Expect> entry : expects.entrySet()) {
            final Expect expect = entry.getValue();
            messages.putAll(expect.messages());
        }

    }


    private abstract class Expect {

        protected final String resource;
        protected final int parseHash;
        protected final IConstraintContext context;

        protected IStrategoTerm errors;
        protected IStrategoTerm warnings;
        protected IStrategoTerm notes;
        protected List<String> exceptions;

        protected Expect(String resource, int parseHash, IStrategoTerm errors, IStrategoTerm warnings,
                IStrategoTerm notes, List<String> exceptions, IConstraintContext context) {
            this.resource = resource;
            this.parseHash = parseHash;
            this.errors = errors;
            this.warnings = warnings;
            this.notes = notes;
            this.exceptions = exceptions != null ? Lists.newArrayList(exceptions) : Lists.newArrayList();
            this.context = context;
        }

        protected FileObject resource() {
            return context.keyResource(resource);
        }

        protected void resultMessages(IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes) {
            this.errors = errors;
            this.warnings = warnings;
            this.notes = notes;
        }

        protected void failMessage(String message) {
            exceptions.add(message);
        }

        abstract void accept(IStrategoTerm result);

        abstract void result(Collection<IMessage> messages, Collection<ISpoofaxAnalyzeUnit> fullResults,
                Collection<ISpoofaxAnalyzeUnitUpdate> updateResults);

        ListMultimap<FileName, IMessage> messages() {
            final ListMultimap<FileName, IMessage> messages = LinkedListMultimap.create();
            messages(MessageSeverity.ERROR, errors, messages);
            messages(MessageSeverity.WARNING, warnings, messages);
            messages(MessageSeverity.NOTE, notes, messages);
            for(String exception : exceptions) {
                messages.put(resource().getName(), MessageFactory.newAnalysisErrorAtTop(resource(), exception, null));
            }
            return messages;
        }

        private void messages(MessageSeverity severity, IStrategoTerm messagesTerm,
                ListMultimap<FileName, IMessage> messages) {
            if(messagesTerm == null) {
                return;
            }
            final FileName fileName = resource().getName();
            if(multifile()) {
                analysisCommon.messages(severity, messagesTerm)
                        .forEach(m -> messages.put(m.source() != null ? m.source().getName() : fileName, m));
            } else {
                analysisCommon.messages(resource(), severity, messagesTerm).forEach(m -> messages.put(fileName, m));
            }
        }

    }

    private class Full extends Expect {

        // 1. initialized by constructor
        private ISpoofaxParseUnit input;
        // 2. initialized by accept
        private IStrategoTerm analyzedAst;
        private IStrategoTerm analysis;

        public Full(String resource, int parseHash, ISpoofaxParseUnit input, IConstraintContext context) {
            super(resource, parseHash, null, null, null, null, context);
            this.input = input;
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Full", 5)) != null) {
                analyzedAst = results.get(0);
                analysis = results.get(1);
                resultMessages(results.get(2), results.get(3), results.get(4));
            } else if(match(result, "Failed", 0) != null) {
                analyzedAst = null;
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
            if(!input.detached()) {
                context.put(resource, parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions);
            }
            fullResults.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(analyzedAst != null, success(messages), true, analyzedAst, messages, -1),
                    context));
        }

    }

    private class UpdateFull extends Expect {

        // 1. initialized by constructor
        private ISpoofaxParseUnit input;
        private IStrategoTerm analyzedAst;
        // 2. initialized by constructor, overwritten by accept
        private IStrategoTerm analysis;

        public UpdateFull(String resource, int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis,
                IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions,
                ISpoofaxParseUnit input, IConstraintContext context) {
            super(resource, parseHash, errors, warnings, notes, exceptions, context);
            this.input = input;
            this.analyzedAst = analyzedAst;
            this.analysis = analysis;
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Update", 4)) != null) {
                analysis = results.get(0);
                resultMessages(results.get(1), results.get(2), results.get(3));
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
            if(!input.detached()) {
                context.put(resource, parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions);
            }
            fullResults.add(unitService.analyzeUnit(input,
                    new AnalyzeContrib(analyzedAst != null, success(messages), true, analyzedAst, messages, -1),
                    context));
        }

    }

    private class Update extends Expect {

        // 1. initialized by constructor
        private IStrategoTerm analyzedAst;
        // 2. initialized by constructor, overwritten by accept
        private IStrategoTerm analysis;

        private Update(String resource, int parseHash, IStrategoTerm analyzedAst, IStrategoTerm analysis,
                IStrategoTerm errors, IStrategoTerm warnings, IStrategoTerm notes, List<String> exceptions,
                IConstraintContext context) {
            super(resource, parseHash, errors, warnings, notes, exceptions, context);
            this.analysis = analysis;
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Update", 4)) != null) {
                analysis = results.get(0);
                resultMessages(results.get(1), results.get(2), results.get(3));
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
            context.put(resource, parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions);
            updateResults.add(unitService.analyzeUnitUpdate(resource(), new AnalyzeUpdateData(messages), context));
        }

    }

    private class ProjectFull extends Expect {

        // 1. initialized by constructor
        private IStrategoTerm analyzedAst;
        // 2. initialized by accept
        private IStrategoTerm analysis;

        public ProjectFull(String resource, int parseHash, IStrategoTerm analyzedAst, IConstraintContext context) {
            super(resource, parseHash, null, null, null, null, context);
            this.analyzedAst = analyzedAst;
        }

        @Override public void accept(IStrategoTerm result) {
            final List<IStrategoTerm> results;
            if((results = match(result, "Full", 5)) != null) {
                analysis = results.get(1);
                resultMessages(results.get(2), results.get(3), results.get(4));
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
            context.put(resource, parseHash, analyzedAst, analysis, errors, warnings, notes, exceptions);
            updateResults.add(unitService.analyzeUnitUpdate(resource(), new AnalyzeUpdateData(messages), context));
        }

    }


    private IStrategoTerm projectAST(String resource) {
        IStrategoTerm ast = termFactory.makeTuple();
        ast = StrategoTermIndices.put(TermIndex.of(resource, 0), ast, termFactory);
        TermOrigin.of(resource).put(ast);
        return ast;
    }


    protected boolean success(Collection<IMessage> messages) {
        return messages.stream().noneMatch(m -> m.severity().equals(MessageSeverity.ERROR));
    }


    protected IStrategoTerm build(String op, IStrategoTerm... subterms) {
        return termFactory.makeAppl(termFactory.makeConstructor(op, subterms.length), subterms);
    }

    protected @Nullable List<IStrategoTerm> match(IStrategoTerm term, String op, int n) {
        if(term == null || !TermUtils.isAppl(term) || !TermUtils.isAppl(term, op, n)) {
            return null;
        }
        return ImmutableList.copyOf(term.getAllSubterms());
    }

}
