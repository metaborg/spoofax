package org.metaborg.core.project;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Interface for retrieving projects of resources.
 */
public interface IProjectService extends Serializable {
    /**
     * Retrieves the project of given resource.
     * 
     * @param resource
     *            Resource to retrieve project for.
     * @return Retrieved project, or null if no project could be retrieved.
     */
    public abstract @Nullable IProject get(FileObject resource);
}
