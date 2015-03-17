package org.metaborg.spoofax.eclipse.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.metaborg.spoofax.eclipse.util.MarkerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

final public class MarkerUpdaterRunnable implements IWorkspaceRunnable {
    private final IEclipseResourceService resourceService;

    private final Iterable<IMessage> extraMessages;
    private final Iterable<IResource> changedResources;
    private final Iterable<AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults;
    private final Iterable<ParseResult<IStrategoTerm>> parseResults;
    private final IProject project;

    MarkerUpdaterRunnable(IEclipseResourceService resourceService, Iterable<IMessage> extraMessages,
        Iterable<IResource> changedResources, Iterable<AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults,
        Iterable<ParseResult<IStrategoTerm>> parseResults, IProject project) {
        this.resourceService = resourceService;

        this.extraMessages = extraMessages;
        this.changedResources = changedResources;
        this.analysisResults = analysisResults;
        this.parseResults = parseResults;
        this.project = project;
    }

    @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
        MarkerUtils.clearAll(project);
        for(IResource resource : changedResources) {
            MarkerUtils.clearAll(resource);
        }

        for(ParseResult<IStrategoTerm> result : parseResults) {
            for(IMessage message : result.messages) {
                final IResource resource = resourceService.unresolve(message.source());
                if(resource == null) {
                    SpoofaxProjectBuilder.logger.error("Cannot create marker for {}", message.source());
                    continue;
                }
                MarkerUtils.createMarker(resource, message);
            }
        }

        for(AnalysisResult<IStrategoTerm, IStrategoTerm> result : analysisResults) {
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : result.fileResults) {
                for(IMessage message : fileResult.messages) {
                    final IResource resource = resourceService.unresolve(message.source());
                    if(resource == null) {
                        SpoofaxProjectBuilder.logger.error("Cannot create marker for {}", message.source());
                        continue;
                    }
                    MarkerUtils.createMarker(resource, message);
                }
            }
        }

        for(IMessage message : extraMessages) {
            final IResource resource = resourceService.unresolve(message.source());
            if(resource == null) {
                SpoofaxProjectBuilder.logger.error("Cannot create marker for {}", message.source());
                continue;
            }
            MarkerUtils.createMarker(resource, message);
        }
    }
}