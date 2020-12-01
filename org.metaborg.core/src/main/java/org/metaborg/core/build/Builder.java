package org.metaborg.core.build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.IdentifiedResourceChange;
import org.metaborg.core.resource.ResourceChange;
import org.metaborg.core.resource.ResourceChangeKind;
import org.metaborg.core.resource.ResourceUtils;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.IInputUnit;
import org.metaborg.core.syntax.IParseUnit;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.transform.ITransformOutput;
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
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
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
    private final IAnalysisResultRequester<I, A> analysisResultRequester;

    private final Provider<IBuildOutputInternal<P, A, AU, T>> buildOutputProvider;


    @Inject public Builder(IResourceService resourceService, ILanguageIdentifierService languageIdentifier,
            ILanguagePathService languagePathService, IUnitService<I, P, A, AU, TP, TA> unitService,
            ISourceTextService sourceTextService, ISyntaxService<I, P> syntaxService, IContextService contextService,
            IAnalysisService<P, A, AU> analysisService, ITransformService<P, A, TP, TA> transformService,
            IParseResultUpdater<P> parseResultUpdater, IAnalysisResultUpdater<P, A> analysisResultUpdater,
            IAnalysisResultRequester<I, A> analysisResultRequester,
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
        this.analysisResultRequester = analysisResultRequester;

        this.buildOutputProvider = buildOutputProvider;
    }


    @Override public IBuildOutput<P, A, AU, T> build(BuildInput input, IProgress progress, ICancel cancel)
            throws InterruptedException {
        cancel.throwIfCancelled();

        final Multimap<ILanguageImpl, IdentifiedResourceChange> changes = ArrayListMultimap.create();
        identifyResources(input.sourceChanges, input, changes, cancel);
        if(changes.size() == 0) {
            // When there are no source changes, keep the old state and skip building.
            final IBuildOutputInternal<P, A, AU, T> buildOutput = buildOutputProvider.get();
            buildOutput.setState(input.state);
            return buildOutput;
        }

        cancel.throwIfCancelled();
        logger.info("Building " + input.project.location());

        final BuildState newState = new BuildState();
        final IBuildOutputInternal<P, A, AU, T> buildOutput = buildOutputProvider.get();
        buildOutput.setState(newState);

        final Iterable<ILanguageImpl> buildOrder = input.buildOrder.buildOrder();
        progress.setWorkRemaining(Iterables.size(buildOrder));
        for(ILanguageImpl language : buildOrder) {
            cancel.throwIfCancelled();

            final LanguageBuildState languageState = input.state.get(resourceService, languageIdentifier, language);
            final Collection<IdentifiedResourceChange> sourceChanges = changes.get(language);
            if(sourceChanges.size() == 0) {
                // When there are no source changes for this language, keep the old state and don't build.
                newState.add(language, languageState);
                continue;
            }
            final Set<FileObject> sourceFiles =
                    sourceChanges.stream().map(irc -> irc.change.resource).collect(Collectors.toSet());

            final Iterable<FileObject> includePaths = input.includePaths.get(language);
            final Iterable<IdentifiedResource> allIncludeFiles = languagePathService.toFiles(includePaths, language);
            // In case source and include files overlap, only keep the include files that are not also source files,
            // otherwise the source files are ignored during transformation.
            final Set<IdentifiedResource> includeFiles = Streams.stream(allIncludeFiles)
                    .filter(ir -> !sourceFiles.contains(ir.resource)).collect(Collectors.toSet());

            final LanguageBuildDiff diff = languageState.diff(changes.get(language), includeFiles);
            final boolean pardoned = input.pardonedLanguages.contains(language);

            final Collection<FileObject> newResources = updateLanguageResources(input, language, diff, buildOutput,
                    pardoned, progress.subProgress(1), cancel);

            final Iterable<ResourceChange> newResourceChanges =
                    ResourceUtils.toChanges(newResources, ResourceChangeKind.Create);
            identifyResources(newResourceChanges, input, changes, cancel);

            newState.add(language, diff.newState);
        }

        final IMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.printSummary();
        }

        return buildOutput;
    }


    private void identifyResources(Iterable<ResourceChange> changes, BuildInput input,
            Multimap<ILanguageImpl, IdentifiedResourceChange> identifiedChanges, ICancel cancel)
            throws InterruptedException {
        final Iterable<ILanguageImpl> languages = input.buildOrder.languages();
        final FileSelector selector = input.selector;
        final FileObject location = input.project.location();

        for(ResourceChange change : changes) {
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
                identifiedChanges.put(identifiedChange.language, identifiedChange);
            }
        }
    }


    private Collection<FileObject> updateLanguageResources(BuildInput input, ILanguageImpl language,
            LanguageBuildDiff diff, IBuildOutputInternal<P, A, AU, T> output, boolean pardoned, IProgress progress,
            ICancel cancel) throws InterruptedException {
        cancel.throwIfCancelled();

        final boolean analyze = input.analyze && analysisService.available(language);
        final boolean transform = input.transform;
        progress.setWorkRemaining(10 + (analyze ? 45 : 0) + (transform ? 45 : 0));

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
                removedResources, extraMessages, success, progress.subProgress(5), cancel);
        // GTODO: when a new context is created, all include files need to be parsed and analyzed in that context, this
        // approach does not do that!
        final Collection<P> includeParseUnits = parse(input, language, includeChanges, pardoned, changedSources,
                removedResources, extraMessages, success, progress.subProgress(5), cancel);
        final Iterable<P> allParseResults = Iterables.concat(sourceParseUnits, includeParseUnits);

        // Segregate by context
        final Multimap<IContext, P> parseUnitsPerContext = ArrayListMultimap.create();
        for(P parseResult : sourceParseUnits) {
            cancel.throwIfCancelled();
            final FileObject resource = parseResult.source();
            final ILanguageImpl langImpl = parseResult.input().langImpl();
            try {
                final IContext context = contextService.get(resource, input.project, langImpl);
                parseUnitsPerContext.put(context, parseResult);
            } catch(ContextException e) {
                final String message = String.format("Failed to retrieve context for parse result of %s", resource);
                printMessage(resource, message, e, input, pardoned);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(resource, "Failed to retrieve context", e));
            }
        }

        // Analyze
        cancel.throwIfCancelled();
        final Multimap<IContext, IAnalyzeResults<A, AU>> allAnalyzeResults;
        if(analyze) {
            // Run analysis
            cancel.throwIfCancelled();
            allAnalyzeResults = analyze(input, language, location, parseUnitsPerContext, includeParseUnits, pardoned,
                    removedResources, extraMessages, success, progress.subProgress(45), cancel);
        } else {
            allAnalyzeResults = ArrayListMultimap.create();
        }

        // Transform
        cancel.throwIfCancelled();
        final Collection<T> allTransformUnits;
        if(transform) {
            allTransformUnits = transform(input, language, location, parseUnitsPerContext, allAnalyzeResults, includes,
                    pardoned, removedResources, extraMessages, success, progress.subProgress(45), cancel);
        } else {
            allTransformUnits = Lists.newLinkedList();
        }

        printMessages(extraMessages, "Something", input, pardoned);

        final List<A> analyzeResults =
                allAnalyzeResults.values().stream().flatMap(r -> r.results().stream()).collect(Collectors.toList());
        final List<AU> analyzeUpdates =
                allAnalyzeResults.values().stream().flatMap(r -> r.updates().stream()).collect(Collectors.toList());
        output.add(success.get(), removedResources, includes, changedSources, allParseResults, analyzeResults,
                analyzeUpdates, allTransformUnits, extraMessages);

        final Collection<FileObject> newResources = Lists.newArrayList();
        for(T transformUnit : allTransformUnits) {
            for(ITransformOutput transformOutput : transformUnit.outputs()) {
                final FileObject outputFile = transformOutput.output();
                if(outputFile != null) {
                    newResources.add(outputFile);
                }
            }
        }
        return newResources;
    }

    private Collection<P> parse(BuildInput input, ILanguageImpl langImpl, Iterable<IdentifiedResourceChange> changes,
            boolean pardoned, Collection<FileObject> changedResources, Set<FileName> removedResources,
            Collection<IMessage> extraMessages, RefBool success, IProgress progress, ICancel cancel)
            throws InterruptedException {
        final int size = Iterables.size(changes);
        progress.setWorkRemaining(size);
        final Collection<P> allParseUnits = Lists.newArrayListWithCapacity(size);
        if(size == 0) {
            return allParseUnits;
        }

        progress.setDescription("Parsing " + size + " file(s) of " + langImpl.belongsTo().name());
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
                    progress.work(1);
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResultUpdater.invalidate(resource);
                    final I inputUnit = unitService.inputUnit(resource, sourceText, langImpl, dialect);
                    final P parseResult = syntaxService.parse(inputUnit, progress.subProgress(1), cancel);
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

    private Multimap<IContext, IAnalyzeResults<A, AU>> analyze(BuildInput input, ILanguageImpl langImpl,
            FileObject location, Multimap<IContext, P> sourceParseUnits, Iterable<P> includeParseUnits,
            boolean pardoned, Set<FileName> removedResources, Collection<IMessage> extraMessages, RefBool success,
            IProgress progress, ICancel cancel) throws InterruptedException {
        final int size = sourceParseUnits.size() + Iterables.size(includeParseUnits);
        final Multimap<IContext, IAnalyzeResults<A, AU>> allAnalyzeUnits = ArrayListMultimap.create();
        if(size == 0) {
            return allAnalyzeUnits;
        }

        final Set<Entry<IContext, Collection<P>>> toAnalyze = sourceParseUnits.asMap().entrySet();
        final int toAnalyzeSize = toAnalyze.size();
        progress.setWorkRemaining(toAnalyzeSize);
        progress.setDescription("Analyzing " + size + " file(s) of " + langImpl.belongsTo().name());
        logger.debug("Analyzing {} parse results in {} context(s)", size, toAnalyzeSize);

        for(Entry<IContext, Collection<P>> entry : toAnalyze) {
            cancel.throwIfCancelled();
            final IContext context = entry.getKey();
            final Iterable<P> parseResults = Iterables.concat(entry.getValue(), includeParseUnits);

            try {
                try(IClosableLock lock = context.write()) {
                    analysisResultUpdater.invalidate(parseResults);
                    final IAnalyzeResults<A, AU> results =
                            analysisService.analyzeAll(parseResults, context, progress.subProgress(1), cancel);
                    allAnalyzeUnits.put(context, results);
                    for(A result : results.results()) {
                        cancel.throwIfCancelled();
                        final boolean noErrors = printMessages(result.messages(), "Analysis", input, pardoned);
                        success.and(noErrors);
                        analysisResultUpdater.update(result, removedResources);
                    }
                    for(AU update : results.updates()) {
                        cancel.throwIfCancelled();
                        final boolean noErrors = printMessages(update.messages(), "Analysis", input, pardoned);
                        success.and(noErrors);
                        // FIXME analysis updates are not supported by analysis updater
                        // analysisResultUpdater.update(update, removedResources);
                    }
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

    private Collection<T> transform(BuildInput input, ILanguageImpl langImpl, FileObject location,
            Multimap<IContext, P> parseUnits, Multimap<IContext, IAnalyzeResults<A, AU>> allAnalysisResults,
            Set<FileName> includeFiles, boolean pardoned, Set<FileName> removedResources,
            Collection<IMessage> extraMessages, RefBool success, IProgress progress, ICancel cancel)
            throws InterruptedException {
        final Collection<T> allTransformUnits = Lists.newArrayList();

        final int numberOfGoals = Iterables.size(input.transformGoals);
        progress.setWorkRemaining(numberOfGoals);
        if(numberOfGoals == 0) {
            return allTransformUnits;
        }
        progress.setDescription("Running " + numberOfGoals + " transformations of " + langImpl.belongsTo().name());
        logger.debug("Running {} transformations", numberOfGoals);


        for(ITransformGoal goal : input.transformGoals) {
            cancel.throwIfCancelled();
            // The two branches of this `if` are almost completely the same,
            // except one works on parse units, the other in analysis units.
            // They should be kept in sync!
            if(transformService.requiresAnalysis(langImpl, goal)) {
                final int size = allAnalysisResults.size();
                if(size == 0) {
                    continue;
                }
                final IProgress transformProgress = progress.subProgress(1);
                transformProgress.setDescription("Compiling " + size + " file(s) with " + goal);
                logger.debug("Compiling {} analysis results", size);

                for(Entry<IContext, Collection<IAnalyzeResults<A, AU>>> entry : allAnalysisResults.asMap().entrySet()) {
                    cancel.throwIfCancelled();
                    final IContext context = entry.getKey();
                    final Collection<IAnalyzeResults<A, AU>> analysisResultList = entry.getValue();

                    if(!transformService.available(context.language(), goal)) {
                        logger.trace("No {} transformation required for {}", goal, context.language());
                        transformProgress.work(analysisResultList.size());
                        continue;
                    }

                    try(IClosableLock lock = context.read()) {
                        final List<FileObject> missingResults = new ArrayList<>();
                        for(IAnalyzeResults<A, AU> analysisResults : analysisResultList) {
                            cancel.throwIfCancelled();

                            List<A> results = new ArrayList<>();
                            results.addAll(analysisResults.results());
                            for(AU update : analysisResults.updates()) {
                                final FileObject source = update.source();
                                try {
                                    if(!source.isFile()) {
                                        continue;
                                    }
                                } catch(FileSystemException ex) {
                                    continue;
                                }

                                A result = analysisResultRequester.get(source);
                                if(result == null) {
                                    missingResults.add(source);
                                } else {
                                    results.add(result);
                                }
                            }

                            for(A analysisResult : results) {

                                final FileObject source = analysisResult.source();

                                if(!analysisResult.valid()) {
                                    logger.warn("Input result for {} is invalid, cannot transform it",
                                            source != null ? source.getName().getPath() : "detached source");
                                    transformProgress.work(1);
                                    continue;
                                }

                                final FileName name = source.getName();

                                if(removedResources.contains(name) || includeFiles.contains(name)) {
                                    // Don't compile removed resources, which the analysis results contain for legacy reasons.
                                    // Don't transform included resources, they should just be parsed and analyzed.
                                    transformProgress.work(1);
                                    continue;
                                }

                                try {
                                    final Collection<TA> transformResults =
                                            transformService.transform(analysisResult, context, goal);
                                    for(TA transformResult : transformResults) {
                                        final boolean noErrors = printMessages(transformResult.messages(),
                                                goal + " transformation", input, pardoned);
                                        success.and(noErrors);
                                        @SuppressWarnings("unchecked") final T genericResult = (T) transformResult;
                                        allTransformUnits.add(genericResult);
                                    }
                                    transformProgress.work(1);
                                } catch(TransformException e) {
                                    final String message =
                                            String.format("Transformation failed unexpectedly for %s", name);
                                    logger.error(message, e);
                                    final boolean noErrors = printMessage(source, message, e, input, pardoned);
                                    success.and(noErrors);
                                    extraMessages.add(MessageFactory.newBuilderErrorAtTop(location,
                                            "Transformation failed unexpectedly", e));
                                }

                            }
                        }
                        if(!missingResults.isEmpty()) {
                            logger.warn("Some files with updated analysis could not be compiled because a full analysis is missing.");
                            logger.warn("Previous compilation results may be out-dated.");
                            logger.warn("Cleaning and building the project fixes this.");
                        }
                    }
                }
            } else {
                final int size = parseUnits.size();
                final IProgress transformProgress = progress.subProgress(1);
                if(size == 0) {
                    continue;
                }
                transformProgress.setDescription("Compiling " + size + " file(s) with " + goal);
                logger.debug("Compiling {} parse results", size);

                for(Entry<IContext, Collection<P>> entry : parseUnits.asMap().entrySet()) {
                    cancel.throwIfCancelled();
                    final IContext context = entry.getKey();
                    final Collection<P> parseResults = entry.getValue();

                    if(!transformService.available(context.language(), goal)) {
                        logger.trace("No {} transformation required for {}", goal, context.language());
                        transformProgress.work(parseResults.size());
                        continue;
                    }

                    for(P parseResult : parseResults) {
                        cancel.throwIfCancelled();

                        final FileObject source = parseResult.source();

                        if(!parseResult.valid()) {
                            logger.warn("Input result for {} is invalid, cannot transform it",
                                    source != null ? source.getName().getPath() : "detached source");
                            transformProgress.work(1);
                            continue;
                        }

                        final FileName name = source.getName();

                        if(removedResources.contains(name) || includeFiles.contains(name)) {
                            // Don't compile removed resources, which the parse results contain for legacy reasons.
                            // Don't transform included resources, they should just be parsed and analyzed.
                            transformProgress.work(1);
                            continue;
                        }

                        try {
                            final Collection<TP> results = transformService.transform(parseResult, context, goal);
                            for(TP result : results) {
                                final boolean noErrors =
                                        printMessages(result.messages(), goal + " transformation", input, pardoned);
                                success.and(noErrors);
                                @SuppressWarnings("unchecked") final T genericResult = (T) result;
                                allTransformUnits.add(genericResult);
                            }
                            transformProgress.work(1);
                        } catch(TransformException e) {
                            final String message = String.format("Transformation failed unexpectedly for %s", name);
                            logger.error(message, e);
                            final boolean noErrors = printMessage(source, message, e, input, pardoned);
                            success.and(noErrors);
                            extraMessages.add(MessageFactory.newBuilderErrorAtTop(location,
                                    "Transformation failed unexpectedly", e));
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

    private boolean printMessage(@Nullable FileObject resource, String message, @Nullable Throwable e, BuildInput input,
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


    @Override public void clean(CleanInput input, IProgress progress, ICancel cancel) throws InterruptedException {
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
                cancel.throwIfCancelled();
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
