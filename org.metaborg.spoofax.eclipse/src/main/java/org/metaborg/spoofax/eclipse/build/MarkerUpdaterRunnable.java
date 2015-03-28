package org.metaborg.spoofax.eclipse.build;

import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoTerm;

final public class MarkerUpdaterRunnable implements IWorkspaceRunnable {
    private static final Logger logger = LoggerFactory.getLogger(MarkerUpdaterRunnable.class);

    private final IEclipseResourceService resourceService;

    private final Set<FileName> removedResources;
    private final Iterable<FileObject> changedResources;
    private final Iterable<IMessage> extraMessages;
    private final Iterable<AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults;
    private final Iterable<ParseResult<IStrategoTerm>> parseResults;
    private final IProject project;


    MarkerUpdaterRunnable(IEclipseResourceService resourceService, Set<FileName> removedResources,
        Iterable<FileObject> changedResources, Iterable<IMessage> extraMessages,
        Iterable<AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults,
        Iterable<ParseResult<IStrategoTerm>> parseResults, IProject project) {
        this.resourceService = resourceService;

        this.removedResources = removedResources;
        this.changedResources = changedResources;
        this.extraMessages = extraMessages;
        this.analysisResults = analysisResults;
        this.parseResults = parseResults;
        this.project = project;
    }

    @Override public void run(IProgressMonitor workspaceMonitor) throws CoreException {
        MarkerUtils.clearAll(project);
        for(FileObject resource : changedResources) {
            final IResource eclipseResource = resourceService.unresolve(resource);
            if(eclipseResource == null) {
                logger.error("Cannot clear markers for {}", resource);
                continue;
            }
            MarkerUtils.clearAll(eclipseResource);
        }

        for(ParseResult<IStrategoTerm> result : parseResults) {
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

        for(AnalysisResult<IStrategoTerm, IStrategoTerm> result : analysisResults) {
            for(AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileResult : result.fileResults) {
                for(IMessage message : fileResult.messages) {
                    final FileObject resource = message.source();
                    if(removedResources.contains(resource.getName())) {
                        // Don't create markers for removed resources. Only analysis results contain removed resources.
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

        for(IMessage message : extraMessages) {
            final FileObject resource = message.source();
            final IResource eclipseResource = resourceService.unresolve(resource);
            if(eclipseResource == null) {
                logger.error("Cannot create marker for {}", resource);
                continue;
            }
            MarkerUtils.createMarker(eclipseResource, message);
        }
    }
}