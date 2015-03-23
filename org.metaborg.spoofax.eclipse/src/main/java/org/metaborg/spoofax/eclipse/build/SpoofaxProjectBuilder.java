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
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
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
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
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
import com.google.common.collect.Iterables;
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
    private final ILanguageIdentifierService languageIdentifier;
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
        this.languageIdentifier = injector.getInstance(ILanguageIdentifierService.class);
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
                            ResourceUtils.projectResources(resourceService, new AllLanguagesFileSelector(
                                languageIdentifier), project);
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
            final Iterable<IResourceChange> changes = changes(project);
            if(!Iterables.isEmpty(changes)) {
                logger.debug("Fully building " + project);
                build(project, changes, monitor);
            }
        } catch(FileSystemException | CoreException e) {
            final String message = String.format("Failed to fully build project %s", project);
            logger.error(message, e);
        }
    }

    private void incrBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) {
        try {
            final Iterable<IResourceChange> changes = changes(delta);
            if(!Iterables.isEmpty(changes)) {
                logger.debug("Incrementally building " + project);
                build(project, changes, monitor);
            }
        } catch(CoreException e) {
            final String message = String.format("Failed to incrementally build project %s", project);
            logger.error(message, e);
        }
    }


    private void build(final IProject project, Iterable<IResourceChange> changes, IProgressMonitor monitor)
        throws CoreException {
        final int numChanges = Iterables.size(changes);
        if(numChanges == 0)
            return;

        final FileObject projectResource = resourceService.resolve(project);
        final Collection<FileObject> changedResources = Sets.newHashSet();
        final Set<FileName> removedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();

        // Parse
        final Collection<ParseResult<IStrategoTerm>> allParseResults = Lists.newArrayListWithExpectedSize(numChanges);
        for(IResourceChange change : changes) {
            final FileObject resource = change.resource();
            final ILanguage language = languageIdentifier.identify(resource);
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
                            Iterables2.<IMessage>empty(), -1, language);
                    allParseResults.add(emptyParseResult);
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResultProcessor.invalidate(resource);
                    final ParseResult<IStrategoTerm> parseResult = syntaxService.parse(sourceText, resource, language);
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
                parseResultProcessor.error(resource, new ParseException(resource, language, e));
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed", e));
                changedResources.add(resource);
            }
        }

        // Segregate by context
        final Multimap<IContext, ParseResult<IStrategoTerm>> allParseResultsPerContext = ArrayListMultimap.create();
        for(ParseResult<IStrategoTerm> parseResult : allParseResults) {
            final FileObject resource = parseResult.source;
            try {
                final IContext context = contextService.get(resource, parseResult.parsedWith);
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

    private Iterable<IResourceChange> changes(IResourceDelta delta) throws CoreException {
        final Collection<IResourceChange> changes = Lists.newLinkedList();
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
                final FileObject resource = resourceService.resolve(innerDelta.getResource());
                if(languageIdentifier.identify(resource) == null) {
                    return true;
                }

                final int eclipseKind = innerDelta.getKind();
                final ResourceChangeKind kind;
                // GTODO: handle move/copies better
                switch(eclipseKind) {
                    case IResourceDelta.ADDED:
                        kind = ResourceChangeKind.Create;
                        break;
                    case IResourceDelta.REMOVED:
                        kind = ResourceChangeKind.Delete;
                        break;
                    case IResourceDelta.CHANGED:
                        kind = ResourceChangeKind.Modify;
                        break;
                    default:
                        final String message = String.format("Unhandled resource delta type: %s", eclipseKind);
                        logger.error(message);
                        throw new SpoofaxRuntimeException(message);
                }

                changes.add(new ResourceChange(resource, kind, null, null));

                return true;
            }
        });
        return changes;
    }

    private Iterable<IResourceChange> changes(IProject project) throws FileSystemException {
        final Collection<FileObject> resources =
            ResourceUtils.projectResources(resourceService, new AllLanguagesFileSelector(languageIdentifier), project);
        final Collection<IResourceChange> changes = Lists.newArrayListWithCapacity(resources.size());
        for(FileObject resource : resources) {
            changes.add(new ResourceChange(resource, ResourceChangeKind.Create, null, null));
        }
        return changes;
    }
}
