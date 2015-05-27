package org.metaborg.spoofax.eclipse.build;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
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
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.processing.BuildInput;
import org.metaborg.spoofax.core.processing.BuildOutput;
import org.metaborg.spoofax.core.processing.ISpoofaxBuilder;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.SpoofaxPlugin;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;
import com.google.inject.Injector;

public class SpoofaxProjectBuilder extends IncrementalProjectBuilder {
    public static final String id = SpoofaxPlugin.id + ".builder";

    private static final Logger logger = LoggerFactory.getLogger(SpoofaxProjectBuilder.class);

    private final IEclipseResourceService resourceService;
    private final ILanguageService languageService;
    private final ISpoofaxBuilder builder;


    public SpoofaxProjectBuilder() {
        final Injector injector = SpoofaxPlugin.injector();
        this.resourceService = injector.getInstance(IEclipseResourceService.class);
        this.builder = injector.getInstance(ISpoofaxBuilder.class);
        this.languageService = injector.getInstance(ILanguageService.class);
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
            final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
                @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                    MarkerUtils.clearAllRec(project);
                    final FileObject location = resourceService.resolve(project);
                    builder.clean(location);
                }
            };
            final IWorkspace workspace = ResourcesPlugin.getWorkspace();
            workspace.run(runnable, project, IWorkspace.AVOID_UPDATE, monitor);
        } catch(CoreException e) {
            final String message = String.format("Cannot clean project %s", project);
            logger.error(message, e);
        }
    }

    private void fullBuild(IProject project, IProgressMonitor monitor) {
        try {
            final FileObject location = resourceService.resolve(project);
            final Collection<IResourceChange> changes = Lists.newLinkedList();
            project.accept(new IResourceVisitor() {
                @Override public boolean visit(IResource eclipseResource) throws CoreException {
                    final FileObject resource = resourceService.resolve(eclipseResource);
                    changes.add(new ResourceChange(resource));
                    return true;
                }
            });
            final BuildInput input = new BuildInput(location, languageService.getAllActive(), changes);
            build(project, input, monitor);
        } catch(CoreException e) {
            final String message = String.format("Failed to fully build project %s", project);
            logger.error(message, e);
        }
    }

    private void incrBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) {
        try {
            final FileObject location = resourceService.resolve(project);
            final Collection<IResourceChange> changes = Lists.newLinkedList();
            delta.accept(new IResourceDeltaVisitor() {
                @Override public boolean visit(IResourceDelta innerDelta) throws CoreException {
                    final IResourceChange change = resourceService.resolve(innerDelta);
                    if(change != null) {
                        changes.add(change);
                    }
                    return true;
                }
            });
            final BuildInput input = new BuildInput(location, languageService.getAllActive(), changes);
            build(project, input, monitor);
        } catch(CoreException e) {
            final String message = String.format("Failed to incrementally build project %s", project);
            logger.error(message, e);
        }
    }

    private void build(final IProject project, final BuildInput input, IProgressMonitor monitor) throws CoreException {
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
                final BuildOutput<IStrategoTerm, IStrategoTerm, IStrategoTerm> output = builder.build(input);

                MarkerUtils.clearAll(project);
                for(FileObject resource : output.changedResources) {
                    final IResource eclipseResource = resourceService.unresolve(resource);
                    if(eclipseResource == null) {
                        logger.error("Cannot clear markers for {}", resource);
                        continue;
                    }
                    MarkerUtils.clearAll(eclipseResource);
                }

                for(ParseResult<IStrategoTerm> result : output.parseResults) {
                    for(IMessage message : result.messages) {
                        final FileObject resource = message.source();
                        final IResource eclipseResource = resourceService.unresolve(resource);
                        if(eclipseResource == null) {
                            logger.error("Cannot create marker for {}", resource);
                            continue;
                        }
                        MarkerUtils.createMarker(eclipseResource, message);
                    }
                }

                for(AnalysisResult<IStrategoTerm, IStrategoTerm> result : output.analysisResults) {
                    for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : result.fileResults) {
                        for(IMessage message : fileResult.messages) {
                            final FileObject resource = message.source();
                            if(output.removedResources.contains(resource.getName())) {
                                // Analysis results contain removed resources, don't create markers for removed
                                // resources.
                                continue;
                            }
                            final IResource eclipseResource = resourceService.unresolve(resource);
                            if(eclipseResource == null) {
                                logger.error("Cannot create marker for {}", resource);
                                continue;
                            }
                            MarkerUtils.createMarker(eclipseResource, message);
                        }
                    }
                }

                for(IMessage message : output.extraMessages) {
                    final FileObject resource = message.source();
                    final IResource eclipseResource = resourceService.unresolve(resource);
                    if(eclipseResource == null) {
                        logger.error("Cannot create marker for {}", resource);
                        continue;
                    }
                    MarkerUtils.createMarker(eclipseResource, message);
                }
            }
        };
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.run(runnable, project, IWorkspace.AVOID_UPDATE, monitor);
    }
}
