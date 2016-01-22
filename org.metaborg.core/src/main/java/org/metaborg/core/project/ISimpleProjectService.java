package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;

/**
 * Specialization of the {@link IProjectService} that supports creating and removing projects. Project instances are
 * returned based on the file names of projects and resources. Nested projects are not supported.
 */
public interface ISimpleProjectService extends IProjectService {
    /**
     * Creates a project at given location.
     * 
     * @return Created project.
     * 
     * @throws MetaborgException
     *             When a project already exists at given location, or when given location is nested in another project.
     */
    IProject create(FileObject location) throws MetaborgException;

    /**
     * Removes given project.
     * 
     * @param project
     *            Project to remove.
     * 
     * @throws MetaborgException
     *             When given project does not exist in this project service.
     */
    void remove(IProject project) throws MetaborgException;
}
