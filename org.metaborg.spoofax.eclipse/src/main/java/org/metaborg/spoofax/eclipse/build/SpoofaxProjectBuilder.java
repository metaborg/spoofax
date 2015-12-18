package org.metaborg.spoofax.eclipse.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.core.analysis.AnalysisException;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.context.ContextUtils;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.dialect.IDialectProcessor;
import org.metaborg.spoofax.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoredDirectories;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseException;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.processing.AnalysisResultProcessor;
import org.metaborg.spoofax.eclipse.processing.ParseResultProcessor;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.ResourceUtils;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private static final Logger logger = LoggerFactory.getLogger(SpoofaxProjectBuilder.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final ILanguageIdentifierService languageIdentifier;
    private final IDialectService dialectService;
    private final IDialectProcessor dialectProcessor;
    private final IContextService contextService;
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final ITermFactoryService termFactoryService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer;
    private final ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer;

    private final ParseResultProcessor parseResultProcessor;
    private final AnalysisResultProcessor analysisResultProcessor;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageService = injector.getInstance(ILanguageService.class);
        this.languageIdentifier = injector.getInstance(ILanguageIdentifierService.class);
        this.dialectService = injector.getInstance(IDialectService.class);
        this.dialectProcessor = injector.getInstance(IDialectProcessor.class);
        this.contextService = injector.getInstance(IContextService.class);
        this.sourceTextService = injector.getInstance(ISourceTextService.class);
        this.syntaxService = injector.getInstance(Key.get(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}));
        this.termFactoryService = injector.getInstance(ITermFactoryService.class);
        this.analyzer =
            injector.getInstance(Key.get(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
        this.transformer =
            injector.getInstance(Key
                .get(new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>() {}));

        this.parseResultProcessor = injector.getInstance(ParseResultProcessor.class);
        this.analysisResultProcessor = injector.getInstance(AnalysisResultProcessor.class);
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        final IProject project = getProject();
        if(kind == FULL_BUILD) {
            fullBuild(project, monitor);
        } else {
            final IResourceDelta delta = getDelta(project);
            if(delta == null) {
                fullBuild(project, monitor);
            } else {
                incrBuild(project, delta, monitor);
            }
        }

        // Return value is used to declare dependencies on other projects, but right now this is
        // not possible in Spoofax, so always return null.
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        clean(getProject(), monitor);
    }


    private void clean(final IProject project, IProgressMonitor monitor) {
        logger.debug("Cleaning project " + project);
        try {
            final IWorkspaceRunnable markerDeleter = new IWorkspaceRunnable() {
                @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                    MarkerUtils.clearAllRec(project);

                    try {
                        final Collection<FileObject> resources =
                            ResourceUtils.projectResources(resourceService, SpoofaxIgnoredDirectories
                                .ignoreFileSelector(new AllLanguagesFileSelector(languageIdentifier)), project);
                        final Set<IContext> contexts =
                            ContextUtils.getAll(resources, languageIdentifier, contextService);
                        for(IContext context : contexts) {
                            context.clean();
                        }
                    } catch(FileSystemException e) {
                        final String message = String.format("Could not clean contexts for {}", project);
                        logger.error(message, e);
                    }
                }
            };
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(markerDeleter, project, IWorkspace.AVOID_UPDATE, monitor);
        } catch(CoreException e) {
            final String message = String.format("Cannot clean project %s", project);
            logger.error(message, e);
        }
    }

    private void fullBuild(IProject project, IProgressMonitor monitor) {
        try {
            final BuildChanges changes = changes(project);
            if(changes.isEmpty()) {
                return;
            }
            logger.debug("Fully building " + project);
            if(!changes.parseTableChanges.isEmpty()) {
                updateDialects(changes.parseTableChanges);
            }
            if(!changes.languageResourceChanges.isEmpty()) {
                updateLanguageResources(project, changes.languageResourceChanges, monitor);
            }
        } catch(CoreException e) {
            final String message = String.format("Failed to fully build project %s", project);
            logger.error(message, e);
        }
    }

    private void incrBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) {
        try {
            final BuildChanges changes = changes(delta);
            if(changes.isEmpty()) {
                return;
            }
            logger.debug("Incrementally building " + project);
            if(!changes.parseTableChanges.isEmpty()) {
                updateDialects(changes.parseTableChanges);
            }
            if(!changes.languageResourceChanges.isEmpty()) {
                updateLanguageResources(project, changes.languageResourceChanges, monitor);
            }
        } catch(CoreException e) {
            final String message = String.format("Failed to incrementally build project %s", project);
            logger.error(message, e);
        }
    }


    private void updateDialects(Collection<IResourceChange> changes) {
        dialectProcessor.update(changes);
    }

    private void updateLanguageResources(final IProject project, Collection<IdentifiedResourceChange> changes,
        IProgressMonitor monitor) throws CoreException {
        final int numChanges = changes.size();

        final FileObject projectResource = resourceService.resolve(project);
        final Collection<FileObject> changedResources = Sets.newHashSet();
        final Set<FileName> removedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();

        // Parse
        logger.debug("Parsing {} resources", numChanges);
        final Collection<ParseResult<IStrategoTerm>> allParseResults = Lists.newArrayListWithExpectedSize(numChanges);
        for(IdentifiedResourceChange identifiedChange : changes) {
            final IResourceChange change = identifiedChange.change;
            final FileObject resource = change.resource();
            final ILanguage language = identifiedChange.language;
            final ILanguage dialect = identifiedChange.dialect;
            final ILanguage parserLanguage = dialect != null ? dialect : language;
            final ResourceChangeKind changeKind = change.kind();

            try {
                if(changeKind == ResourceChangeKind.Delete) {
                    parseResultProcessor.remove(resource);
                    // Don't add resource as changed when it has been deleted, because it does not exist any more
                    // according to Eclipse and will be null.
                    removedResources.add(resource.getName());
                    // LEGACY: add parse result with empty tuple, to indicate to analysis that this resource was
                    // removed. There is special handling in updating the analysis result processor, the marker
                    // updater, and the compiler, to exclude removed resources.
                    final ParseResult<IStrategoTerm> emptyParseResult =
                        new ParseResult<IStrategoTerm>(termFactoryService.getGeneric().makeTuple(), resource,
                            Iterables2.<IMessage>empty(), -1, language, dialect);
                    allParseResults.add(emptyParseResult);
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResultProcessor.invalidate(resource);
                    final ParseResult<IStrategoTerm> parseResult =
                        syntaxService.parse(sourceText, resource, parserLanguage);
                    allParseResults.add(parseResult);
                    parseResultProcessor.update(resource, parseResult);
                    changedResources.add(resource);
                }
            } catch(ParseException e) {
                final String message = String.format("Parsing failed for %s", resource);
                logger.error(message, e);
                parseResultProcessor.error(resource, e);
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed", e));
                changedResources.add(resource);
            } catch(IOException e) {
                final String message = String.format("Parsing failed for %s", resource);
                logger.error(message, e);
                parseResultProcessor.error(resource, new ParseException(resource, parserLanguage, e));
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed", e));
                changedResources.add(resource);
            }
        }

        // Segregate by context
        final Multimap<IContext, ParseResult<IStrategoTerm>> allParseResultsPerContext = ArrayListMultimap.create();
        for(ParseResult<IStrategoTerm> parseResult : allParseResults) {
            final FileObject resource = parseResult.source;
            try {
                final IContext context = contextService.get(resource, parseResult.language);
                allParseResultsPerContext.put(context, parseResult);
            } catch(ContextException e) {
                final String message = String.format("Could not retrieve context for parse result of %s", resource);
                logger.error(message, e);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(resource, "Failed to retrieve context", e));
            }
        }


        // Analyze
        final Map<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> allAnalysisResults =
            Maps.newHashMapWithExpectedSize(allParseResultsPerContext.keySet().size());
        for(Entry<IContext, Collection<ParseResult<IStrategoTerm>>> entry : allParseResultsPerContext.asMap()
            .entrySet()) {
            final IContext context = entry.getKey();
            final Iterable<ParseResult<IStrategoTerm>> parseResults = entry.getValue();

            try {
                synchronized(context) {
                    analysisResultProcessor.invalidate(parseResults);
                    final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
                        analyzer.analyze(parseResults, context);
                    analysisResultProcessor.update(analysisResult, removedResources);
                    allAnalysisResults.put(context, analysisResult);
                }
                // GTODO: also update messages for affected sources
            } catch(AnalysisException e) {
                logger.error("Analysis failed", e);
                analysisResultProcessor.error(parseResults, e);
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(projectResource, "Analysis failed", e));
            }
        }


        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Compile atomically using a workspace runnable, because new files will be generated, which need to be batched
        // up and built in the next project build.
        final IWorkspaceRunnable compilerRunnable =
            new CompilerRunnable(transformer, projectResource, removedResources, allAnalysisResults.entrySet(),
                extraMessages);
        workspace.run(compilerRunnable, project, IWorkspace.AVOID_UPDATE, monitor);

        // Update markers atomically using a workspace runnable, which batches resource and marker changes, to prevent
        // flashing/jumping markers.
        final IWorkspaceRunnable markerUpdater =
            new MarkerUpdaterRunnable(resourceService, removedResources, changedResources, extraMessages,
                allAnalysisResults.values(), allParseResults, project);
        workspace.run(markerUpdater, project, IWorkspace.AVOID_UPDATE, monitor);
    }


    private BuildChanges changes(IResourceDelta delta) throws CoreException {
        final Collection<IdentifiedResourceChange> languageResourceChanges = Lists.newLinkedList();
        final Collection<IResourceChange> parseTableChanges = Lists.newLinkedList();
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
                final IResourceChange change = resourceService.resolve(innerDelta);
                if(change == null) {
                    return true;
                }

                final FileObject resource = change.resource();
                if(SpoofaxIgnoredDirectories.ignoreResource(resource)) {
                    return false;
                }

                if(resource.getName().getExtension().equals("tbl")) {
                    parseTableChanges.add(change);
                    return true;
                }

                final ILanguage language = languageIdentifier.identify(resource);
                if(language != null) {
                    final ILanguage base = dialectService.getBase(language);
                    if(base == null) {
                        languageResourceChanges.add(new IdentifiedResourceChange(change, language, null));
                    } else {
                        languageResourceChanges.add(new IdentifiedResourceChange(change, base, language));
                    }
                    return true;
                }
                return true;
            }
        });
        return new BuildChanges(parseTableChanges, languageResourceChanges);
    }

    private BuildChanges changes(IProject project) throws CoreException {
        final Collection<IdentifiedResourceChange> languageResourceChanges = Lists.newLinkedList();
        final Collection<IResourceChange> parseTableChanges = Lists.newLinkedList();
        project.accept(new IResourceVisitor() {
            @Override public boolean visit(IResource eclipseResource) throws CoreException {
                final FileObject resource = resourceService.resolve(eclipseResource);
                if(SpoofaxIgnoredDirectories.ignoreResource(resource)) {
                    return false;
                }

                if(resource.getName().getExtension().equals("tbl")) {
                    parseTableChanges.add(new ResourceChange(resource));
                    return true;
                }

                final ILanguage language = languageIdentifier.identify(resource);
                if(language != null) {
                    final ILanguage base = dialectService.getBase(language);
                    if(base == null) {
                        languageResourceChanges.add(new IdentifiedResourceChange(new ResourceChange(resource),
                            language, null));
                    } else {
                        languageResourceChanges.add(new IdentifiedResourceChange(new ResourceChange(resource), base,
                            language));
                    }
                    return true;
                }

                return true;
            }
        });

        return new BuildChanges(parseTableChanges, languageResourceChanges);
    }
}
