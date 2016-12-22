package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Creates a project from a single file with the parent directory as project location (if possible, otherwise just
 * the file) and no config (== null).
 * Never returns null for the project.
 * Doesn't cache projects.
 */
public class SingleFileProjectService implements IProjectService {

    @Override
    public IProject get(FileObject resource) {
        try {
            // project location should be a directory (otherwise building gave errors), so take parent dir (if possible)
            if (resource.isFile()) {
                return new Project(resource.getParent(), null);
            } else {
                return new Project(resource, null);
            }
        } catch (FileSystemException e) {
            return new Project(resource, null);
        }
    }
}
