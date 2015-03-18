package org.metaborg.spoofax.eclipse.util;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.language.LanguageFileSelector;
import org.metaborg.spoofax.eclipse.resource.IEclipseResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class WorkspaceUtils {
    private static final Logger logger = LoggerFactory.getLogger(WorkspaceUtils.class);

    /**
     * Returns all resources in the workspace identified by given language.
     * 
     * @param resourceService
     *            Resource service
     * @param languageIdentifier
     *            Language identifier service
     * @param language
     *            Language to get resources for.
     * @param workspaceRoot
     *            Eclipse workspace root resource.
     * @return Collection of all resources in the workspace identified by given language.
     * @throws FileSystemException
     *             When an error occurs while finding all resources.
     */
    public static Collection<IResource> languageResources(IEclipseResourceService resourceService,
        ILanguageIdentifierService languageIdentifier, ILanguage language, IWorkspaceRoot workspaceRoot)
        throws FileSystemException {
        final FileSelector selector = new LanguageFileSelector(languageIdentifier, language);
        final Collection<IResource> eclipseResources = Lists.newLinkedList();
        // GTODO: should this include hidden projects?
        for(IProject project : workspaceRoot.getProjects()) {
            if(project.exists() && project.isOpen()) {
                final FileObject projectResource = resourceService.resolve(project);
                final FileObject[] resources = projectResource.findFiles(selector);
                for(FileObject resource : resources) {
                    final IResource eclipseResource = resourceService.unresolve(resource);
                    if(eclipseResource != null) {
                        eclipseResources.add(eclipseResource);
                    } else {
                        logger.error("Cannot unresolve {}", resource);
                    }
                }
            }
        }
        return eclipseResources;
    }
}
