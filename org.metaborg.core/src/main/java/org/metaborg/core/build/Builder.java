package org.metaborg.core.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.analysis.AnalysisException;
import org.metaborg.core.analysis.AnalysisFileResult;
import org.metaborg.core.analysis.AnalysisResult;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.ContextUtils;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.AllLanguagesFileSelector;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.messages.IMessage;
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
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.syntax.ParseResult;
import org.metaborg.core.transform.ITransformer;
import org.metaborg.core.transform.ITransformerGoal;
import org.metaborg.core.transform.TransformResult;
import org.metaborg.core.transform.TransformerException;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.resource.DefaultFileSelectInfo;
import org.metaborg.util.resource.FilterFileSelector;
import org.metaborg.util.resource.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

/**
 * Builder implementation.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public class Builder<P, A, T> implements IBuilder<P, A, T> {
    private static final Logger logger = LoggerFactory.getLogger(Builder.class);

    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifier;
    private final IDialectProcessor dialectProcessor;
    private final IContextService contextService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<P> syntaxService;
    private final IAnalysisService<P, A> analyzer;
    private final ITransformer<P, A, T> transformer;

    private final IParseResultUpdater<P> parseResultProcessor;
    private final IAnalysisResultUpdater<P, A> analysisResultProcessor;


    @Inject public Builder(IResourceService resourceService, ILanguageIdentifierService languageIdentifier,
        IDialectProcessor dialectProcessor, IContextService contextService, ISourceTextService sourceTextService,
        ISyntaxService<P> syntaxService, IAnalysisService<P, A> analyzer, ITransformer<P, A, T> transformer,
        IParseResultUpdater<P> parseResultProcessor, IAnalysisResultUpdater<P, A> analysisResultProcessor) {
        this.resourceService = resourceService;
        this.languageIdentifier = languageIdentifier;
        this.dialectProcessor = dialectProcessor;
        this.contextService = contextService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.analyzer = analyzer;
        this.transformer = transformer;

        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;
    }


    @Override public IBuildOutput<P, A, T> build(BuildInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken) {
        final Iterable<ILanguage> languages = input.buildOrder.languages();

        final Collection<ResourceChange> parseTableChanges = Lists.newLinkedList();
        final Multimap<ILanguage, IdentifiedResourceChange> changes = ArrayListMultimap.create();

        final FileObject location = input.project.location();
        for(ResourceChange change : input.resourceChanges) {
            final FileObject resource = change.resource;
            final FileSelector selector = input.selector;
            if(selector != null) {
                final FileSelectInfo info = new DefaultFileSelectInfo(location, resource, -1);
                try {
                    if(!selector.includeFile(info)) {
                        continue;
                    }
                } catch(Exception e) {
                    // Ignore exception, just include file.
                }
            }

            // GTODO: abstract this into dialect processor, because it is implementation-specific.
            if(resource.getName().getExtension().equals("tbl")) {
                parseTableChanges.add(change);
                continue;
            }

            final IdentifiedResource identifiedResource = languageIdentifier.identifyToResource(resource, languages);
            if(identifiedResource != null) {
                final IdentifiedResourceChange identifiedChange =
                    new IdentifiedResourceChange(change, identifiedResource);
                changes.put(identifiedChange.language, identifiedChange);
            }
        }

        updateDialectResources(parseTableChanges);

        if(changes.size() == 0) {
            // When there are no source changes, keep the old state and skip building.
            return new BuildOutput<>(input.state);
        }

        final BuildState newState = new BuildState();
        final BuildOutput<P, A, T> buildOutput = new BuildOutput<>(newState);
        for(ILanguage language : input.buildOrder.buildOrder()) {
            final LanguageBuildState languageState = input.state.get(resourceService, languageIdentifier, language);
            final Collection<IdentifiedResourceChange> sourceChanges = changes.get(language);
            if(sourceChanges.size() == 0) {
                // When there are no source changes for this language, keep the old state and don't build.
                newState.add(language, languageState);
                continue;
            }

            final Iterable<FileObject> includeLocations = input.includeLocations.get(language);
            final Collection<IdentifiedResource> includeFiles = Lists.newLinkedList();
            for(FileObject includeLocation : includeLocations) {
                for(FileObject includeResource : ResourceUtils.expand(includeLocation)) {
                    final IdentifiedResource identifiedResource =
                        languageIdentifier.identifyToResource(includeResource, Iterables2.singleton(language));
                    if(identifiedResource != null) {
                        includeFiles.add(identifiedResource);
                    }
                }
            }
            final LanguageBuildDiff diff = languageState.diff(changes.get(language), includeFiles);
            updateLanguageResources(input, language, diff, buildOutput);
            newState.add(language, diff.newState);
        }

        return buildOutput;
    }

    private void updateDialectResources(Collection<ResourceChange> changes) {
        if(changes.isEmpty()) {
            return;
        }

        dialectProcessor.update(changes);
    }

    private void updateLanguageResources(BuildInput input, ILanguage language, LanguageBuildDiff diff,
        BuildOutput<P, A, T> output) {
        logger.debug("Building " + input.project.location());

        final Iterable<IdentifiedResourceChange> sourceChanges = diff.sourceChanges;
        final int numSourceChanges = Iterables.size(sourceChanges);

        final Iterable<IdentifiedResourceChange> includeChanges = diff.includeChanges;
        final int numIncludeChanges = Iterables.size(includeChanges);
        final Set<FileName> includedResources = Sets.newHashSet();
        for(IdentifiedResourceChange includeChange : includeChanges) {
            includedResources.add(includeChange.change.resource.getName());
        }

        final FileObject location = input.project.location();
        final Collection<FileObject> changedResources = Sets.newHashSet();
        final Set<FileName> removedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();

        // Parse
        final int numChanges = numSourceChanges + numIncludeChanges;
        logger.debug("Parsing {} sources, {} includes", numSourceChanges, numIncludeChanges);
        final Collection<ParseResult<P>> sourceParseResults =
            parse(input, language, sourceChanges, numSourceChanges, changedResources, removedResources, extraMessages);
        // GTODO: when a new context is created, all include files need to be parsed and analyzed, this approach
        // does not do that!
        final Collection<ParseResult<P>> includeParseResults =
            parse(input, language, includeChanges, numIncludeChanges, changedResources, removedResources, extraMessages);
        final Iterable<ParseResult<P>> allParseResults = Iterables.concat(sourceParseResults, includeParseResults);

        // Segregate by context
        final Multimap<IContext, ParseResult<P>> sourceResultsPerContext = ArrayListMultimap.create();
        for(ParseResult<P> parseResult : sourceParseResults) {
            final FileObject resource = parseResult.source;
            try {
                final IContext context = contextService.get(resource, parseResult.language);
                sourceResultsPerContext.put(context, parseResult);
            } catch(ContextException e) {
                final String message = String.format("Failed to retrieve context for parse result of %s", resource);
                printMessage(resource, message, e, input, language);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(resource, "Failed to retrieve context", e));
            }
        }

        // Analyze
        final Collection<AnalysisResult<P, A>> allAnalysisResults =
            analyze(input, language, location, sourceResultsPerContext, includeParseResults, numChanges,
                removedResources, extraMessages);

        // Compile
        final Collection<TransformResult<AnalysisFileResult<P, A>, T>> allTransformResults =
            compile(input, language, location, allAnalysisResults, includedResources, numSourceChanges,
                removedResources, extraMessages);

        printMessages(extraMessages, "Something", input, language);

        output.add(removedResources, includedResources, changedResources, allParseResults, allAnalysisResults,
            allTransformResults, extraMessages);
    }

    private Collection<ParseResult<P>> parse(BuildInput input, ILanguage language,
        Iterable<IdentifiedResourceChange> changes, int size, Collection<FileObject> changedResources,
        Set<FileName> removedResources, Collection<IMessage> extraMessages) {
        final Collection<ParseResult<P>> allParseResults = Lists.newArrayListWithExpectedSize(size);
        if(size == 0) {
            return allParseResults;
        }

        for(IdentifiedResourceChange identifiedChange : changes) {
            final ResourceChange change = identifiedChange.change;
            final FileObject resource = change.resource;
            final ILanguage dialect = identifiedChange.dialect;
            final ILanguage parserLanguage = dialect != null ? dialect : language;
            final ResourceChangeKind changeKind = change.kind;

            try {
                if(changeKind == ResourceChangeKind.Delete) {
                    parseResultProcessor.remove(resource);
                    removedResources.add(resource.getName());
                    // LEGACY: add empty parse result, to indicate to analysis that this resource was
                    // removed. There is special handling in updating the analysis result processor, the marker
                    // updater, and the compiler, to exclude removed resources.
                    final ParseResult<P> emptyParseResult =
                        syntaxService.emptyParseResult(resource, parserLanguage, dialect);
                    allParseResults.add(emptyParseResult);
                    // Don't add resource as changed when it has been deleted, because it does not exist any more.
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResultProcessor.invalidate(resource);
                    final ParseResult<P> parseResult = syntaxService.parse(sourceText, resource, parserLanguage, null);
                    printMessages(parseResult.messages, "Parsing", input, language);
                    allParseResults.add(parseResult);
                    parseResultProcessor.update(resource, parseResult);
                    changedResources.add(resource);
                }
            } catch(ParseException e) {
                final String message = String.format("Parsing failed unexpectedly for %s", resource);
                printMessage(resource, message, e, input, language);
                parseResultProcessor.error(resource, e);
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed unexpectedly", e));
                changedResources.add(resource);
            } catch(IOException e) {
                final String message = String.format("Parsing failed unexpectedly for %s", resource);
                printMessage(resource, message, e, input, language);
                parseResultProcessor.error(resource, new ParseException(resource, parserLanguage, e));
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed unexpectedly", e));
                changedResources.add(resource);
            }
        }
        return allParseResults;
    }

    private Collection<AnalysisResult<P, A>> analyze(BuildInput input, ILanguage language, FileObject location,
        Multimap<IContext, ParseResult<P>> sourceParseResults, Iterable<ParseResult<P>> includeParseResults, int size,
        Set<FileName> removedResources, Collection<IMessage> extraMessages) {
        final Collection<AnalysisResult<P, A>> allAnalysisResults = Lists.newArrayListWithExpectedSize(size);
        if(size == 0) {
            return allAnalysisResults;
        }

        for(Entry<IContext, Collection<ParseResult<P>>> entry : sourceParseResults.asMap().entrySet()) {
            final IContext context = entry.getKey();
            final Iterable<ParseResult<P>> parseResults = Iterables.concat(entry.getValue(), includeParseResults);

            try {
                synchronized(context) {
                    analysisResultProcessor.invalidate(parseResults);
                    final AnalysisResult<P, A> analysisResult = analyzer.analyze(parseResults, context);
                    for(AnalysisFileResult<P, A> fileResult : analysisResult.fileResults) {
                        printMessages(fileResult.messages, "Analysis", input, language);
                    }
                    analysisResultProcessor.update(analysisResult, removedResources);
                    allAnalysisResults.add(analysisResult);
                }
                // GTODO: also update messages for affected sources
            } catch(AnalysisException e) {
                final String message = "Analysis failed unexpectedly";
                printMessage(message, e, input, language);
                analysisResultProcessor.error(parseResults, e);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(location, message, e));
            }
        }
        return allAnalysisResults;
    }

    private Collection<TransformResult<AnalysisFileResult<P, A>, T>> compile(BuildInput input, ILanguage language,
        FileObject location, Collection<AnalysisResult<P, A>> allAnalysisResults, Set<FileName> includeFiles, int size,
        Set<FileName> removedResources, Collection<IMessage> extraMessages) {
        final Collection<TransformResult<AnalysisFileResult<P, A>, T>> allTransformResults =
            Lists.newArrayListWithExpectedSize(size);
        if(size == 0) {
            return allTransformResults;
        }

        for(AnalysisResult<P, A> analysisResult : allAnalysisResults) {
            final IContext context = analysisResult.context;
            synchronized(context) {
                for(AnalysisFileResult<P, A> fileResult : analysisResult.fileResults) {
                    final FileObject resource = fileResult.source;
                    final FileName name = resource.getName();
                    if(removedResources.contains(name) || includeFiles.contains(name)) {
                        // Don't compile removed resources, which the analysis results contain for legacy reasons.
                        // Don't transform included resources, they should just be parsed and analyzed.
                        continue;
                    }

                    if(fileResult.result == null) {
                        logger.warn("Input result for {} is null, cannot transform it", resource);
                        continue;
                    }

                    try {
                        for(ITransformerGoal goal : input.transformGoals) {
                            if(!transformer.available(goal, context)) {
                                logger.trace("No {} transformation required for {}", goal, context.language());
                                continue;
                            }
                            final TransformResult<AnalysisFileResult<P, A>, T> result =
                                transformer.transform(fileResult, context, goal);
                            printMessages(result.messages, goal + " transformation", input, language);
                            allTransformResults.add(result);
                        }
                    } catch(TransformerException e) {
                        final String message = String.format("Transformation failed unexpectedly for %s", name);
                        printMessage(resource, message, e, input, language);
                        extraMessages.add(MessageFactory.newBuilderErrorAtTop(location,
                            "Transformation failed unexpectedly", e));
                    }
                }
                // GTODO: also compile any affected sources
            }
        }
        return allTransformResults;
    }

    private void printMessages(Iterable<IMessage> messages, String phase, BuildInput input, ILanguage language) {
        final IBuildMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            for(IMessage message : messages) {
                printer.print(message);
            }
        }

        if(input.throwOnErrors && !input.pardonedLanguages.contains(language)
            && MessageUtils.containsSeverity(messages, MessageSeverity.ERROR)) {
            throw new MetaborgRuntimeException(phase + " produced errors");
        }
    }

    private void printMessage(FileObject resource, String message, @Nullable Throwable e, BuildInput input,
        ILanguage language) {
        final IBuildMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.print(resource, message, e);
        }

        if(input.throwOnErrors && !input.pardonedLanguages.contains(language)) {
            throw new MetaborgRuntimeException(message, e);
        }
    }

    private void printMessage(String message, @Nullable Throwable e, BuildInput input, ILanguage language) {
        final IBuildMessagePrinter printer = input.messagePrinter;
        if(printer != null) {
            printer.print(input.project, message, e);
        }

        if(input.throwOnErrors && !input.pardonedLanguages.contains(language)) {
            throw new MetaborgRuntimeException(message, e);
        }
    }


    @Override public void clean(CleanInput input, IProgressReporter progressReporter,
        ICancellationToken cancellationToken) {
        final FileObject location = input.project.location();
        logger.debug("Cleaning " + location);

        try {
            final FileSelector selector =
                new FilterFileSelector(new AllLanguagesFileSelector(languageIdentifier), input.selector);
            final FileObject[] resources = location.findFiles(selector);
            final Set<IContext> contexts =
                ContextUtils.getAll(Iterables2.from(resources), languageIdentifier, contextService);
            for(IContext context : contexts) {
                context.clean();
            }
        } catch(FileSystemException e) {
            final String message = String.format("Could not clean contexts for {}", location);
            logger.error(message, e);
        }
    }
}
