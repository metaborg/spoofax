package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

/**
 * A project.
 *
 * A language specification project should implement the {@link ILanguageSpec} interface;
 * or implement the {@link ILanguageSpecService} for the project type.
 */
public class Project implements IProject {

    private final FileObject location;
    
    
    public Project(FileObject location) {
        this.location = location;
    }


    /**
     * {@inheritDoc}
     */
    @Override public FileObject location() {
        return location;
    }
}
