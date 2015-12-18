package org.metaborg.spoofax.eclipse.util;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ResourceUtils {
    private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

    /**
     * Returns all resources in the workspace using given file selector.
     * 
     * @param resourceService
     *            Resource service
     * @param selector
     *            Selector used to filter resources.
     * @param workspaceRoot
     *            Eclipse workspace root resource.
     * @return Collection of all resources in the workspace.
     * @throws FileSystemException
     *             When an error occurs while finding all resources.
     */
    public static Collection<FileObject> workspaceResources(IEclipseResourceService resourceService,
        FileSelector selector, IWorkspaceRoot workspaceRoot) throws FileSystemException {
        final Collection<FileObject> resources = Lists.newLinkedList();
        // GTODO: should this include hidden projects?
        for(IProject project : workspaceRoot.getProjects()) {
            if(project.exists() && project.isOpen()) {
                final FileObject projectResource = resourceService.resolve(project);
                final FileObject[] projectResources = projectResource.findFiles(selector);
                Collections.addAll(resources, projectResources);
            }
        }
        return resources;
    }

    /**
     * Returns all resources in the workspace using given file selector.
     * 
     * @param resourceService
     *            Resource service
     * @param selector
     *            Selector used to filter resources.
     * @param workspaceRoot
     *            Eclipse workspace root resource.
     * @return Collection of all resources in the workspace.
     * @throws FileSystemException
     *             When an error occurs while finding all resources.
     */
    public static Collection<FileObject> projectResources(IEclipseResourceService resourceService,
        FileSelector selector, IProject project) throws FileSystemException {
        final Collection<FileObject> resources = Lists.newLinkedList();
        if(project.exists() && project.isOpen()) {
            final FileObject projectResource = resourceService.resolve(project);
            final FileObject[] projectResources = projectResource.findFiles(selector);
            Collections.addAll(resources, projectResources);
        }
        return resources;
    }

    /**
     * Converts Apache VFS resources on the Eclipse filesystem, to Eclipse resources.
     * 
     * @param resourceService
     *            Resource service used to convert resources.
     * @param resources
     *            The VFS resources to convert.
     * @return Eclipse resources.
     */
    public static Collection<IResource> toEclipseResources(IEclipseResourceService resourceService,
        Collection<FileObject> resources) {
        final Collection<IResource> eclipseResources = Lists.newArrayListWithExpectedSize(resources.size());
        for(FileObject resource : resources) {
            final IResource eclipseResource = resourceService.unresolve(resource);
            if(eclipseResource != null) {
                eclipseResources.add(eclipseResource);
            } else {
                logger.error("Cannot unresolve {}", resource);
            }
        }
        return eclipseResources;
    }
}
