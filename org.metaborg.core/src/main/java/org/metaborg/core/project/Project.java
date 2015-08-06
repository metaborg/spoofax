package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;

public class Project implements IProject {
    private final FileObject location;
    
    
    public Project(FileObject location) {
        this.location = location;
    }


    @Override public FileObject location() {
        return location;
    }
}
