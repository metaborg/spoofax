package org.metaborg.spoofax.eclipse.build;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.AllLanguagesFileSelector;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.messages.MessageFactory;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.text.ISourceTextService;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
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
    private final ISourceTextService sourceTextService;
    private final ISyntaxService<IStrategoTerm> syntaxService;
    private final ITermFactoryService termFactoryService;
    private final IAnalysisService<IStrategoTerm, IStrategoTerm> analyzer;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.languageIdentifier = injector.getInstance(ILanguageIdentifierService.class);
        this.sourceTextService = injector.getInstance(ISourceTextService.class);
        this.syntaxService = injector.getInstance(Key.get(new TypeLiteral<ISyntaxService<IStrategoTerm>>() {}));
        this.termFactoryService = injector.getInstance(ITermFactoryService.class);
        this.analyzer =
            injector.getInstance(Key.get(new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {}));
    }


    /**
     * Adds this builder to given project. Does nothing if builder has already been added to the project.
     * 
     * @param project
     *            Project to add the builder to.
     * @throws CoreException
     *             when {@link IProject#getDescription} throws a CoreException.
     */
    public static void addTo(IProject project) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        if(builderIndex(builders) == -1) {
            final ICommand newBuilder = projectDesc.newCommand();
            newBuilder.setBuilderName(id);
            final ICommand[] newBuilders = ArrayUtils.add(builders, 0, newBuilder);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    /**
     * Removes this builder from given project. Does nothing if the builder has not been added to the project.
     * 
     * @param project
     *            Project to remove the builder from.
     * @throws CoreException
     *             when {@link IProject#getDescription} or {@link IProject#setDescription} throws a CoreException.
     */
    public static void removeFrom(IProject project) throws CoreException {
        final IProjectDescription projectDesc = project.getDescription();
        final ICommand[] builders = projectDesc.getBuildSpec();
        final int builderIndex = builderIndex(builders);
        if(builderIndex != -1) {
            final ICommand[] newBuilders = ArrayUtils.remove(builders, builderIndex);
            projectDesc.setBuildSpec(newBuilders);
            project.setDescription(projectDesc, null);
        }
    }

    private static int builderIndex(ICommand[] builders) throws CoreException {
        for(int i = 0; i < builders.length; ++i) {
            final ICommand builder = builders[i];
            if(builder.getBuilderName().equals(id)) {
                return i;
            }
        }
        return -1;
    }


    @Override protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
        throws CoreException {
        if(kind == FULL_BUILD) {
            fullBuild(getProject(), monitor);
        } else {
            final IResourceDelta delta = getDelta(getProject());
            if(delta == null) {
                fullBuild(getProject(), monitor);
            } else {
                incrBuild(getProject(), delta, monitor);
            }
        }

        // Return value is used to declare dependencies on other projects, but right now this is
        // not possible in Spoofax, so always return null.
        return null;
    }

    @Override protected void clean(IProgressMonitor monitor) throws CoreException {
        clean(getProject(), monitor);
    }

    private void clean(final IProject project, IProgressMonitor monitor) throws CoreException {
        logger.debug("Cleaning project " + project);
        final IWorkspaceRunnable markerDeleter = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor monitor) throws CoreException {
                MarkerUtils.clearAllRec(project);
            }
        };
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(markerDeleter, project, IWorkspace.AVOID_UPDATE, monitor);
    }

    private void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException {
        logger.debug("Fully building " + project);
        try {
            final Iterable<IResourceChange> changes = changes(project);
            build(project, changes);
        } catch(FileSystemException e) {
            throw new CoreException(StatusUtils.error("Cannot retrieve resources for full build", e));
        }
    }

    private void incrBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        logger.debug("Incrementally building " + project);
        final Iterable<IResourceChange> changes = changes(delta);
        build(project, changes);
    }

    private void build(final IProject project, Iterable<IResourceChange> changes) throws CoreException {
        if(Iterables.isEmpty(changes))
            return;

        final FileObject projectResource = resourceService.resolve(project);
        final Set<IResource> changedResources = Sets.newHashSet();
        final Collection<IMessage> extraMessages = Lists.newLinkedList();

        final Multimap<ILanguage, IResourceChange> changesPerLang = LinkedHashMultimap.create();
        for(IResourceChange change : changes) {
            final ILanguage language = languageIdentifier.identify(change.resource());
            if(language != null) {
                changesPerLang.put(language, change);
            }
        }
        final int numLangs = changesPerLang.keySet().size();
        final int numResources = changesPerLang.values().size();

        // Parse
        final Multimap<ILanguage, ParseResult<IStrategoTerm>> allParseResults =
            LinkedHashMultimap.create(numLangs, numResources / numLangs);
        for(Entry<ILanguage, IResourceChange> entry : changesPerLang.entries()) {
            final ILanguage language = entry.getKey();
            final FileObject resource = entry.getValue().resource();
            final ResourceChangeKind changeKind = entry.getValue().kind();

            try {
                final ParseResult<IStrategoTerm> parseResult;
                if(changeKind == ResourceChangeKind.Delete) {
                    // For compatibility with existing Spoofax languages, turn deleted resources
                    // into a parse result with () as result.
                    final ITermFactory termFactory = termFactoryService.get(language);
                    parseResult =
                        new ParseResult<IStrategoTerm>(termFactory.makeTuple(), resource, Iterables2.<IMessage>empty(),
                            0l, language);
                } else {
                    final String sourceText = sourceTextService.text(resource);
                    parseResult = syntaxService.parse(sourceText, resource, language);
                }

                allParseResults.put(language, parseResult);
            } catch(IOException e) {
                extraMessages.add(MessageFactory.newParseErrorAtTop(resource, "Parsing failed: " + e.getMessage()));
            }

            changedResources.add(resourceService.unresolve(resource));
        }

        // Analyze
        final Multimap<ILanguage, AnalysisResult<IStrategoTerm, IStrategoTerm>> allAnalysisResults =
            LinkedHashMultimap.create(numLangs, numResources / numLangs);
        for(Entry<ILanguage, Collection<ParseResult<IStrategoTerm>>> entry : allParseResults.asMap().entrySet()) {
            final ILanguage language = entry.getKey();
            final Iterable<ParseResult<IStrategoTerm>> parseResults = entry.getValue();

            // GTODO: create only one context per language.
            final IContext context = new SpoofaxContext(language, projectResource);

            try {
                final AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
                    analyzer.analyze(parseResults, context);
                allAnalysisResults.put(language, analysisResult);
            } catch(SpoofaxException e) {
                extraMessages.add(MessageFactory.newAnalysisErrorAtTop(projectResource,
                    "Analysis failed: " + e.getMessage()));
            }
        }

        // Update markers
        final IWorkspaceRunnable markerUpdater = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor monitor) throws CoreException {
                MarkerUtils.clearAll(project);
                for(IResource resource : changedResources) {
                    MarkerUtils.clearAll(resource);
                }

                for(ParseResult<IStrategoTerm> result : allParseResults.values()) {
                    for(IMessage message : result.messages) {
                        final IResource resource = resourceService.unresolve(message.source());
                        if(resource == null) {
                            logger.error("Cannot create marker for " + message.source());
                            continue;
                        }
                        MarkerUtils.createMarker(resource, message);
                    }
                }

                for(AnalysisResult<IStrategoTerm, IStrategoTerm> result : allAnalysisResults.values()) {
                    for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : result.fileResults) {
                        for(IMessage message : fileResult.messages()) {
                            final IResource resource = resourceService.unresolve(message.source());
                            if(resource == null) {
                                logger.error("Cannot create marker for " + message.source());
                                continue;
                            }
                            MarkerUtils.createMarker(resource, message);
                        }
                    }
                }
            }
        };
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(markerUpdater, project, IWorkspace.AVOID_UPDATE, null);
    }

    private Iterable<IResourceChange> changes(IResourceDelta delta) throws CoreException {
        final Collection<IResourceChange> changes = Lists.newLinkedList();
        delta.accept(new IResourceDeltaVisitor() {
            @Override public boolean visit(IResourceDelta delta) throws CoreException {
                final FileObject resource = resourceService.resolve(delta.getResource());
                if(languageIdentifier.identify(resource) == null) {
                    return true;
                }

                final int eclipseKind = delta.getKind();
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
                        throw new CoreException(StatusUtils.error("Unhandled resource delta type: " + eclipseKind));
                }

                changes.add(new ResourceChange(resource, kind, null, null));

                return true;
            }
        });
        return changes;
    }

    private Iterable<IResourceChange> changes(IProject project) throws FileSystemException {
        final Collection<IResourceChange> changes = Lists.newLinkedList();
        final FileObject projectResource = resourceService.resolve(project);
        final FileObject[] resources = projectResource.findFiles(new AllLanguagesFileSelector(languageIdentifier));
        for(FileObject resource : resources) {
            changes.add(new ResourceChange(resource, ResourceChangeKind.Create, null, null));
        }
        return changes;
    }
}
