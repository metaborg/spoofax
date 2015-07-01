package org.metaborg.spoofax.core.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.context.ContextUtils;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.dialect.IDialectProcessor;
import org.metaborg.spoofax.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.messages.MessageSeverity;
import org.metaborg.spoofax.core.messages.MessageUtils;
import org.metaborg.spoofax.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.parse.IParseResultUpdater;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoredDirectories;
import org.metaborg.spoofax.core.source.ISourceTextService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.metaborg.spoofax.core.transform.TransformerException;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.resource.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class Builder<P, A, T> implements IBuilder<P, A, T> {
    private static final Logger logger = LoggerFactory.getLogger(Builder.class);

    private final ILanguageIdentifierService languageIdentifier;
    private final IDialectService dialectService;
    private final IDialectProcessor dialectProcessor;
    private final IContextService contextService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<P> syntaxService;
    private final IAnalysisService<P, A> analyzer;
    private final ITransformer<P, A, T> transformer;

    private final IParseResultUpdater<P> parseResultProcessor;
    private final IAnalysisResultUpdater<P, A> analysisResultProcessor;


    public Builder(ILanguageIdentifierService languageIdentifier, IDialectService dialectService,
        IDialectProcessor dialectProcessor, IContextService contextService, ISourceTextService sourceTextService,
        ISyntaxService<P> syntaxService, IAnalysisService<P, A> analyzer, ITransformer<P, A, T> transformer,
        IParseResultUpdater<P> parseResultProcessor, IAnalysisResultUpdater<P, A> analysisResultProcessor) {
        this.languageIdentifier = languageIdentifier;
        this.dialectService = dialectService;
        this.dialectProcessor = dialectProcessor;
        this.contextService = contextService;
        this.sourceTextService = sourceTextService;
        this.syntaxService = syntaxService;
        this.analyzer = analyzer;
        this.transformer = transformer;

        this.parseResultProcessor = parseResultProcessor;
        this.analysisResultProcessor = analysisResultProcessor;
    }


    @Override public IBuildOutput<P, A, T> build(BuildInput input) {
        final Iterable<ILanguage> languages = input.buildOrder.languages();

        final Collection<IResourceChange> parseTableChanges = Lists.newLinkedList();
        final Multimap<ILanguage, IdentifiedResourceChange> changes = ArrayListMultimap.create();

        for(IResourceChange change : input.resourceChanges) {
            final FileObject resource = change.resource();
            if(SpoofaxIgnoredDirectories.ignoreResource(resource)) {
                continue;
            }

            if(resource.getName().getExtension().equals("tbl")) {
                parseTableChanges.add(change);
                continue;
            }

            final IdentifiedResourceChange identifiedChange = identify(change, languages);
            if(identifiedChange != null) {
                changes.put(identifiedChange.language, identifiedChange);
            }
        }

        updateDialectResource(parseTableChanges);

        final BuildOutput<P, A, T> buildOutput = new BuildOutput<P, A, T>();
        for(ILanguage language : input.buildOrder.buildOrder()) {
            final Iterable<FileObject> includeLocations = input.includeLocations.get(language);
            final Collection<IdentifiedResourceChange> includes = Lists.newLinkedList();
            for(FileObject includeLocation : includeLocations) {
                for(FileObject includeResource : ResourceUtils.expand(includeLocation)) {
                    final IResourceChange change = new ResourceChange(includeResource);
                    final IdentifiedResourceChange identifiedChange = identify(change, language);
                    if(identifiedChange != null) {
                        includes.add(identifiedChange);
                    }
                }
            }
            updateLanguageResources(input, language, changes.get(language), includes, buildOutput);
        }

        return buildOutput;
    }

    @Override public void clean(FileObject location) {
        logger.debug("Cleaning " + location);

        try {
            final FileSelector selector =
                SpoofaxIgnoredDirectories.ignoreFileSelector(new AllLanguagesFileSelector(languageIdentifier));
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


    private void updateDialectResource(Collection<IResourceChange> changes) {
        if(changes.isEmpty()) {
            return;
        }

        dialectProcessor.update(changes);
    }

    private void updateLanguageResources(BuildInput input, ILanguage language,
        Collection<IdentifiedResourceChange> changes, Collection<IdentifiedResourceChange> includes,
        BuildOutput<P, A, T> output) {
        if(changes.isEmpty()) {
            return;
        }

        logger.debug("Building " + input.project.location());

        final FileObject location = input.project.location();
        final int numChanges = changes.size();
        final Collection<FileObject> changedResources = Sets.newHashSet();
        final Set<FileName> removedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();

        // Parse
        logger.debug("Parsing {} resources", numChanges);
        final Collection<ParseResult<P>> allParseResults = Lists.newArrayListWithExpectedSize(numChanges);
        for(IdentifiedResourceChange identifiedChange : Iterables.concat(changes, includes)) {
            final IResourceChange change = identifiedChange.change;
            final FileObject resource = change.resource();
            // final ILanguage language = identifiedChange.language;
            final ILanguage dialect = identifiedChange.dialect;
            final ILanguage parserLanguage = dialect != null ? dialect : language;
            final ResourceChangeKind changeKind = change.kind();

            try {
                if(changeKind == ResourceChangeKind.Delete) {
                    parseResultProcessor.remove(resource);
                    // Don't add resource as changed when it has been deleted, because it does not exist any more.
                    removedResources.add(resource.getName());
                    // LEGACY: add empty parse result, to indicate to analysis that this resource was
                    // removed. There is special handling in updating the analysis result processor, the marker
                    // updater, and the compiler, to exclude removed resources.
                    final ParseResult<P> emptyParseResult =
                        syntaxService.emptyParseResult(resource, parserLanguage, dialect);
                    allParseResults.add(emptyParseResult);
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

        // Segregate by context
        final Multimap<IContext, ParseResult<P>> allParseResultsPerContext = ArrayListMultimap.create();
        for(ParseResult<P> parseResult : allParseResults) {
            final FileObject resource = parseResult.source;
            try {
                final IContext context = contextService.get(resource, parseResult.language);
                allParseResultsPerContext.put(context, parseResult);
            } catch(ContextException e) {
                final String message = String.format("Failed to retrieve context for parse result of %s", resource);
                printMessage(resource, message, e, input, language);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(resource, "Failed to retrieve context", e));
            }
        }


        // Analyze
        final Collection<AnalysisResult<P, A>> allAnalysisResults = Lists.newArrayListWithExpectedSize(numChanges);
        for(Entry<IContext, Collection<ParseResult<P>>> entry : allParseResultsPerContext.asMap().entrySet()) {
            final IContext context = entry.getKey();
            final Iterable<ParseResult<P>> parseResults = entry.getValue();

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

        // Compile
        final Set<FileName> includeSet = Sets.newHashSet();
        for(IdentifiedResourceChange include : includes) {
            includeSet.add(include.change.resource().getName());
        }
        final Collection<TransformResult<AnalysisFileResult<P, A>, T>> allTransformResults =
            Lists.newArrayListWithExpectedSize(numChanges);
        final CompileGoal compileGoal = new CompileGoal();
        for(AnalysisResult<P, A> analysisResult : allAnalysisResults) {
            final IContext context = analysisResult.context;
            if(!transformer.available(compileGoal, context)) {
                logger.trace("No compilation required for {}", context.language());
                continue;
            }
            synchronized(context) {
                for(AnalysisFileResult<P, A> fileResult : analysisResult.fileResults) {
                    final FileObject resource = fileResult.source;
                    final FileName name = resource.getName();
                    if(includeSet.contains(name) || removedResources.contains(name)) {
                        // Don't transform included resources, they should just be parsed and analyzed.
                        // Don't compile removed resources, which the analysis results contain for legacy reasons.
                        continue;
                    }

                    if(fileResult.result == null) {
                        logger.warn("Input result for {} is null, cannot transform it", resource);
                        continue;
                    }

                    try {
                        final TransformResult<AnalysisFileResult<P, A>, T> result =
                            transformer.transform(fileResult, context, compileGoal);
                        printMessages(result.messages, "Transformation", input, language);
                        allTransformResults.add(result);
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

        printMessages(extraMessages, "Something", input, language);

        output.add(removedResources, changedResources, allParseResults, allAnalysisResults, allTransformResults,
            extraMessages);
    }

    private @Nullable IdentifiedResourceChange identify(IResourceChange change, Iterable<ILanguage> languages) {
        final FileObject resource = change.resource();
        final ILanguage language = languageIdentifier.identify(resource, languages);
        if(language != null) {
            final ILanguage base = dialectService.getBase(language);
            if(base == null) {
                return new IdentifiedResourceChange(change, language, null);
            } else {
                return new IdentifiedResourceChange(change, base, language);
            }
        }
        return null;
    }

    private @Nullable IdentifiedResourceChange identify(IResourceChange change, ILanguage language) {
        final FileObject resource = change.resource();
        if(languageIdentifier.identify(resource, language)) {
            final ILanguage base = dialectService.getBase(language);
            if(base == null) {
                return new IdentifiedResourceChange(change, language, null);
            } else {
                return new IdentifiedResourceChange(change, base, language);
            }
        }
        return null;
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
            throw new SpoofaxRuntimeException(phase + " produced errors");
        }
    }

    private void printMessage(FileObject resource, String message, @Nullable Throwable e, BuildInput input,
        ILanguage language) {
        input.messagePrinter.print(resource, message, e);
        if(input.throwOnErrors && !input.pardonedLanguages.contains(language)) {
            throw new SpoofaxRuntimeException(message, e);
        }
    }

    private void printMessage(String message, @Nullable Throwable e, BuildInput input, ILanguage language) {
        input.messagePrinter.print(input.project, message, e);
        if(input.throwOnErrors && !input.pardonedLanguages.contains(language)) {
            throw new SpoofaxRuntimeException(message, e);
        }
    }
}
