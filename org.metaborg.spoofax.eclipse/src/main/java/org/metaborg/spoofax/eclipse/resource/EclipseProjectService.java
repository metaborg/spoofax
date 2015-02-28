package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.spoofax.core.project.IProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class EclipseProjectService implements IProjectService {
    private static final Logger logger = LoggerFactory.getLogger(EclipseProjectService.class);

    private final IEclipseResourceService resourceService;


    @Inject public EclipseProjectService(IEclipseResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public IProject get(FileObject resource) {
        final IResource eclipseResource = resourceService.unresolve(resource);
        if(eclipseResource == null) {
            logger.error("Cannot get project, {} is not an Eclipse resource", resource);
            return null;
        }
        final org.eclipse.core.resources.IProject eclipseProject = eclipseResource.getProject();
        if(eclipseProject == null) {
            logger.error("Cannot get project, {} is the Eclipse workspace root", resource);
            return null;
        }
        final FileObject location = resourceService.resolve(eclipseProject);
        return new EclipseProject(location);
    }
}
