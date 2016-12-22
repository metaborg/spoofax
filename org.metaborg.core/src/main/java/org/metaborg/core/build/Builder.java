package org.metaborg.core.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzeResults;
import org.metaborg.core.analysis.IAnalyzeUnit;
import org.metaborg.core.analysis.IAnalyzeUnitUpdate;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.ContextUtils;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.LanguagesFileSelector;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.IMessagePrinter;
import org.metaborg.core.messages.MessageFactory;
import org.metaborg.core.messages.MessageSeverity;
import org.metaborg.core.messages.MessageUtils;
import org.metaborg.core.processing.ICancellationToken;
import org.metaborg.core.processing.IProgressReporter;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.IdentifiedResourceChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.transform.ITransformService;
import org.metaborg.core.transform.ITransformUnit;
import org.metaborg.core.transform.TransformException;
import org.metaborg.core.unit.IUnitService;
import org.metaborg.util.RefBool;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Builder implementation.
 * 
 * @param <P>
 *            Type of parse units.
 * @param <A>
 *            Type of analyze units.
 * @param <AU>
 *            Type of analyze unit updates.
 * @param <T>
 *            Type of transform units with any input.
 * @param <TP>
 *            Type of transform units with parse units as input.
 * @param <TA>
 *            Type of transform units with analyze units as input.
 */
public class Builder<I extends IInputUnit, P extends IParseUnit, A extends IAnalyzeUnit, AU extends IAnalyzeUnitUpdate, T extends ITransformUnit<?>, TP extends ITransformUnit<P>, TA extends ITransformUnit<A>>
    implements IBuilder<P, A, AU, T> {
    private static final ILogger logger = LoggerUtils.logger(Builder.class);

    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;
    private final ILanguagePathService languagePathService;
    private final IUnitService<I, P, A, AU, TP, TA> unitService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<I, P> syntaxService;
    private final IContextService contextService;
    private final IAnalysisService<P, A, AU> analysisService;
    private final ITransformService<P, A, TP, TA> transformService;

    private final IParseResultUpdater<P> parseResultUpdater;
    private final IAnalysisResultUpdater<P, A> analysisResultUpdater;

    private final Provider<IBuildOutputInternal<P, A, AU, T>> buildOutputProvider;


    @Inject public Builder(IResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        ILanguagePathService languagePathService, IUnitService<I, P, A, AU, TP, TA> unitService,
        ISourceTextService sourceTextService, ISyntaxService<I, P> syntaxService, IContextService contextService,
        IAnalysisService<P, A, AU> analysisService, ITransformService<P, A, TP, TA> transformService,
        IParseResultUpdater<P> parseResultUpdater, IAnalysisResultUpdater<P, A> analysisResultUpdater,
        Provider<IBuildOutputInternal<P, A, AU, T>> buildOutputProvider) {
        this.resourceService = resourceService;
        this.languageIdentifier = languageIdentifier;
        this.languagePathService = languagePathService;
        this.unitService = unitService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.contextService = contextService;
        this.analysisService = analysisService;
        this.transformService = transformService;

        this.parseResultUpdater = parseResultUpdater;
        this.analysisResultUpdater = analysisResultUpdater;

        this.buildOutputProvider = buildOutputProvider;
    }


    @Override public IBuildOutput<P, A, AU, T> build(BuildInput input, IProgressReporter progressReporter,
        ICancellationToken cancel) throws InterruptedException {
        cancel.throwIfCancelled();

        final Iterable<ILanguageImpl> languages = input.buildOrder.languages();

        final Multimap<ILanguageImpl, IdentifiedResourceChange> changes = ArrayListMultimap.create();

        final FileSelector selector = input.selector;
        final FileObject location = input.project.location();
        for(ResourceChange change : input.sourceChanges) {
            cancel.throwIfCancelled();

            final FileObject resource = change.resource;

            if(selector != null) {
                try {
                    if(!FileSelectorUtils.include(selector, resource, location)) {
                        continue;
                    }
                } catch(FileSystemException e) {
                    logger.error("Error determining if {} should be ignored from the build, including it", e, resource);
                }
            }

            final IdentifiedResource identifiedResource = languageIdentifier.identifyToResource(resource, languages);
            if(identifiedResource != null) {
                final IdentifiedResourceChange identifiedChange =
                    new IdentifiedResourceChange(change, identifiedResource);
                changes.put(identifiedChange.language, identifiedChange);
            }
        }

        if(changes.size() == 0) {
            // When there are no source changes, keep the old state and skip building.
            final IBuildOutputInternal<P, A, AU, T> buildOutput = buildOutputProvider.get();
            buildOutput.setState(input.state);
            return buildOutput;
        }

        logger.info("Building " + input.project.location());

        cancel.throwIfCancelled();

        final BuildState newState = new BuildState();
        final IBuildOutputInternal<P, A, AU, T> buildOutput = buildOutputProvider.get();
        buildOutput.setState(newState);
        for(ILanguageImpl language : input.buildOrder.buildOrder()) {
            cancel.throwIfCancelled();

            final LanguageBuildState languageState = input.state.get(resourceService, languageIdentifier, language);
            final Collection<IdentifiedResourceChange> sourceChanges = changes.get(language);
            if(sourceChanges.size() == 0) {
                // When there are no source changes for this language, keep the old state and don't build.
                newState.add(language, languageState);
                continue;
            }

            final Iterable<FileObject> includePaths = input.includePaths.get(language);
            final Iterable<IdentifiedResource> includeFiles = languagePathService.toFiles(includePaths, language);
            final LanguageBuildDiff diff = languageState.diff(changes.get(language), includeFiles);
            final boolean pardoned = input.pardonedLanguages.contains(language);
            updateLanguageResources(input, language, diff, buildOutput, pardoned, cancel);
            newState.add(language, diff.newState);
        }

        final IMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.printSummary();
        }

        return buildOutput;
    }


    private void updateLanguageResources(BuildInput input, ILanguageImpl language, LanguageBuildDiff diff,
        IBuildOutputInternal<P, A, AU, T> output, boolean pardoned, ICancellationToken cancel)
        throws InterruptedException {

        final Iterable<IdentifiedResourceChange> sourceChanges = diff.sourceChanges;
        final Iterable<IdentifiedResourceChange> includeChanges = diff.includeChanges;
        final Set<FileName> includes = Sets.newHashSet();
        for(IdentifiedResourceChange includeChange : includeChanges) {
            includes.add(includeChange.change.resource.getName());
        }
        final FileObject location = input.project.location();
        final Collection<FileObject> changedSources = Sets.newHashSet();
        final Set<FileName> removedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();
        final RefBool success = new RefBool(true);

        logger.info("Building {} sources, {} includes of {}", Iterables.size(sourceChanges),
            Iterables.size(includeChanges), language);

        // Parse
        cancel.throwIfCancelled();
        final Collection<P> sourceParseUnits = parse(input, language, sourceChanges, pardoned, changedSources,
            removedResources, extraMessages, success, cancel);
        // GTODO: when a new context is created, all include files need to be parsed and analyzed in that context, this
        // approach does not do that!
        final Collection<P> includeParseUnits = parse(input, language, includeChanges, pardoned, changedSources,
            removedResources, extraMessages, success, cancel);
        final Iterable<P> allParseResults = Iterables.concat(sourceParseUnits, includeParseUnits);

        // Analyze
        cancel.throwIfCancelled();
        final Multimap<IContext, A> allAnalyzeUnits;
        final Collection<AU> allAnalyzeUpdates = Lists.newArrayList();
        boolean analyze = input.analyze && analysisService.available(language);
        if(analyze) {
            // Segregate by context
            final Multimap<IContext, P> parseUnitsPerContext = ArrayListMultimap.create();
            for(P parseResult : sourceParseUnits) {
                cancel.throwIfCancelled();
                final FileObject resource = parseResult.source();
                final ILanguageImpl langImpl = parseResult.input().langImpl();
                try {
                    if(contextService.available(langImpl)) {
                        final IContext context = contextService.get(resource, input.project, langImpl);
                        parseUnitsPerContext.put(context, parseResult);
                    }
                } catch(ContextException e) {
                    final String message = String.format("Failed to retrieve context for parse result of %s", resource);
                    printMessage(resource, message, e, input, pardoned);
                    extraMessages.add(MessageFactory.newAnalysisErrorAtTop(resource, "Failed to retrieve context", e));
                }
            }

            // Run analysis
            cancel.throwIfCancelled();
            allAnalyzeUnits = analyze(input, location, parseUnitsPerContext, includeParseUnits, pardoned,
                allAnalyzeUpdates, removedResources, extraMessages, success, cancel);
        } else {
            allAnalyzeUnits = ArrayListMultimap.create();
        }

        // Transform
        cancel.throwIfCancelled();
        final Collection<T> allTransformUnits;
        if(input.transform) {
            allTransformUnits = transform(input, location, allAnalyzeUnits, includes, pardoned, removedResources,
                extraMessages, success, cancel);
        } else {
            allTransformUnits = Lists.newLinkedList();
        }

        printMessages(extraMessages, "Something", input, pardoned);

        output.add(success.get(), removedResources, includes, changedSources, allParseResults, allAnalyzeUnits.values(),
            allAnalyzeUpdates, allTransformUnits, extraMessages);
    }

    private Collection<P> parse(BuildInput input, ILanguageImpl langImpl, Iterable<IdentifiedResourceChange> changes,
        boolean pardoned, Collection<FileObject> changedResources, Set<FileName> removedResources,
        Collection<IMessage> extraMessages, RefBool success, ICancellationToken cancel) throws InterruptedException {
        final int size = Iterables.size(changes);
        final Collection<P> allParseUnits = Lists.newArrayListWithCapacity(size);
        if(size == 0) {
            return allParseUnits;
        }

        logger.debug("Parsing {} resources", size);

        for(IdentifiedResourceChange identifiedChange : changes) {
            cancel.throwIfCancelled();
            final ResourceChange change = identifiedChange.change;
            final FileObject resource = change.resource;
            final ILanguageImpl dialect = identifiedChange.dialect;
            final ResourceChangeKind changeKind = change.kind;

            try {
                if(changeKind == ResourceChangeKind.Delete) {
                    parseResultUpdater.remove(resource);
                    removedResources.add(resource.getName());
                    // LEGACY: add empty parse result, to indicate to analysis that this resource was
                    // removed. There is special handling in updating the analysis result processor, the marker
                    // updater, and the compiler, to exclude removed resources.
                    final I inputUnit = unitService.emptyInputUnit(resource, langImpl, dialect);
                    final P emptyParseResult = unitService.emptyParseUnit(inputUnit);
                    allParseUnits.add(emptyParseResult);
                    // Don't add resource as changed when it has been deleted, because it does not exist any more.
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResultUpdater.invalidate(resource);
                    final I inputUnit = unitService.inputUnit(resource, sourceText, langImpl, dialect);
                    final P parseResult = syntaxService.parse(inputUnit);
                    final boolean noErrors = printMessages(parseResult.messages(), "Parsing", input, pardoned);
                    success.and(noErrors);
                    allParseUnits.add(parseResult);
                    parseResultUpdater.update(resource, parseResult);
                    changedResources.add(resource);
                }
            } catch(ParseException e) {
                final String message = logger.format("Parsing {} failed unexpectedly", resource);
                final boolean noErrors = printMessage(resource, message, e, input, pardoned);
                success.and(noErrors);
                parseResultUpdater.error(resource, e);
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed unexpectedly", e));
                changedResources.add(resource);
            } catch(IOException e) {
                final String message = logger.format("Getting source text for {} failed unexpectedly", resource);
                final boolean noErrors = printMessage(resource, message, e, input, pardoned);
                success.and(noErrors);
                final I inputUnit = unitService.emptyInputUnit(resource, langImpl, dialect);
                parseResultUpdater.error(resource, new ParseException(inputUnit, e));
                extraMessages
                    .add(MessageFactory.newParseErrorAtTop(resource, "Getting source text failed unexpectedly", e));
                changedResources.add(resource);
            }
        }
        return allParseUnits;
    }

    private Multimap<IContext, A> analyze(BuildInput input, FileObject location, Multimap<IContext, P> sourceParseUnits,
        Iterable<P> includeParseUnits, boolean pardoned, Collection<AU> analyzeUpdates, Set<FileName> removedResources,
        Collection<IMessage> extraMessages, RefBool success, ICancellationToken cancel) throws InterruptedException {
        final int size = sourceParseUnits.size() + Iterables.size(includeParseUnits);
        final Multimap<IContext, A> allAnalyzeUnits = ArrayListMultimap.create();
        if(size == 0) {
            return allAnalyzeUnits;
        }

        logger.debug("Analyzing {} parse results in {} context(s)", size, sourceParseUnits.keySet().size());

        for(Entry<IContext, Collection<P>> entry : sourceParseUnits.asMap().entrySet()) {
            cancel.throwIfCancelled();
            final IContext context = entry.getKey();
            final Iterable<P> parseResults = Iterables.concat(entry.getValue(), includeParseUnits);

            try {
                try(IClosableLock lock = context.write()) {
                    analysisResultUpdater.invalidate(parseResults);
                    final IAnalyzeResults<A, AU> results = analysisService.analyzeAll(parseResults, context);
                    for(A result : results.results()) {
                        cancel.throwIfCancelled();
                        final boolean noErrors = printMessages(result.messages(), "Analysis", input, pardoned);
                        success.and(noErrors);
                        analysisResultUpdater.update(result, removedResources);
                        allAnalyzeUnits.put(context, result);
                    }
                    analyzeUpdates.addAll(results.updates());
                } finally {
                    context.persist();
                }
            } catch(AnalysisException e) {
                final String message = "Analysis failed unexpectedly";
                final boolean noErrors = printMessage(message, e, input, pardoned);
                success.and(noErrors);
                analysisResultUpdater.error(parseResults, e);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(location, message, e));
            } catch(IOException e) {
                final String message = "Persisting analysis data failed unexpectedly";
                final boolean noErrors = printMessage(message, e, input, pardoned);
                success.and(noErrors);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(location, message, e));
            }
        }
        return allAnalyzeUnits;
    }

    private Collection<T> transform(BuildInput input, FileObject location, Multimap<IContext, A> allAnalysisUnits,
        Set<FileName> includeFiles, boolean pardoned, Set<FileName> removedResources,
        Collection<IMessage> extraMessages, RefBool success, ICancellationToken cancel) throws InterruptedException {
        int size = allAnalysisUnits.size();
        final Collection<T> allTransformUnits = Lists.newArrayListWithCapacity(size);
        if(size == 0) {
            return allTransformUnits;
        }

        logger.debug("Compiling {} analysis results", size);

        for(Entry<IContext, Collection<A>> entry : allAnalysisUnits.asMap().entrySet()) {
            cancel.throwIfCancelled();
            final IContext context = entry.getKey();
            final Iterable<A> analysisResults = entry.getValue();
            try(IClosableLock lock = context.read()) {
                for(A analysisResult : analysisResults) {
                    cancel.throwIfCancelled();

                    final FileObject resource = analysisResult.source();
                    final FileName name = resource.getName();

                    if(removedResources.contains(name) || includeFiles.contains(name)) {
                        // Don't compile removed resources, which the analysis results contain for legacy reasons.
                        // Don't transform included resources, they should just be parsed and analyzed.
                        continue;
                    }

                    if(!analysisResult.valid()) {
                        logger.warn("Input result for {} is invalid, cannot transform it", resource);
                        continue;
                    }

                    for(ITransformGoal goal : input.transformGoals) {
                        cancel.throwIfCancelled();
                        if(!transformService.available(context, goal)) {
                            logger.trace("No {} transformation required for {}", goal, context.language());
                            continue;
                        }
                        try {
                            final Collection<TA> results = transformService.transform(analysisResult, context, goal);
                            for(TA result : results) {
                                final boolean noErrors =
                                    printMessages(result.messages(), goal + " transformation", input, pardoned);
                                success.and(noErrors);
                                @SuppressWarnings("unchecked") final T genericResult = (T) result;
                                allTransformUnits.add(genericResult);
                            }
                        } catch(TransformException e) {
                            final String message = String.format("Transformation failed unexpectedly for %s", name);
                            logger.error(message, e);
                            final boolean noErrors = printMessage(resource, message, e, input, pardoned);
                            success.and(noErrors);
                            extraMessages.add(
                                MessageFactory.newBuilderErrorAtTop(location, "Transformation failed unexpectedly", e));
                        }
                    }
                }
                // GTODO: also compile any affected sources
            }
        }
        return allTransformUnits;
    }

    private boolean printMessages(Iterable<IMessage> messages, String phase, BuildInput input, boolean pardoned) {
        final IMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            for(IMessage message : messages) {
                printer.print(message, pardoned);
            }
        }

        final boolean failed = !pardoned && MessageUtils.containsSeverity(messages, MessageSeverity.ERROR);
        if(input.throwOnErrors && failed) {
            throw new MetaborgRuntimeException(phase + " produced errors");
        }
        return !failed;
    }

    private boolean printMessage(FileObject resource, String message, @Nullable Throwable e, BuildInput input,
        boolean pardoned) {
        final IMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.print(resource, message, e, pardoned);
        }

        if(input.throwOnErrors && !pardoned) {
            throw new MetaborgRuntimeException(message, e);
        }
        return pardoned;
    }

    private boolean printMessage(String message, @Nullable Throwable e, BuildInput input, boolean pardoned) {
        final IMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.print(input.project, message, e, pardoned);
        }

        if(input.throwOnErrors && !pardoned) {
            throw new MetaborgRuntimeException(message, e);
        }
        return pardoned;
    }


    @Override public void clean(CleanInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken) throws InterruptedException {
        final FileObject location = input.project.location();
        logger.debug("Cleaning {}", location);

        FileSelector selector = new LanguagesFileSelector(languageIdentifier, input.languages);
        if(input.selector != null) {
            selector = FileSelectorUtils.and(selector, input.selector);
        }
        try {
            final FileObject[] resources = location.findFiles(selector);
            if(resources == null) {
                return;
            }
            final Set<IContext> contexts =
                ContextUtils.getAll(Iterables2.from(resources), input.project, languageIdentifier, contextService);
            for(IContext context : contexts) {
                try {
                    context.reset();
                } catch(IOException e) {
                    logger.error("Could not clean {}", e, context);
                }
            }
        } catch(FileSystemException e) {
            logger.error("Could not clean contexts at {}", e, location);
        }
    }
}
