package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.project.IProject;

public class EclipseProject implements IProject {
    private final FileObject location;


    public EclipseProject(FileObject location) {
        this.location = location;
    }


    @Override public FileObject location() {
        return location;
    }
}
