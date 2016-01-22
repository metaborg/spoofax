package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for retrieving projects of resources.
 */
public interface IProjectService {
    /**
     * Retrieves the project of given resource.
     * 
     * @param resource
     *            Resource to retrieve project for.
     * @return Retrieved project, or null if no project could be retrieved.
     */
    @Nullable IProject get(FileObject resource);
}
