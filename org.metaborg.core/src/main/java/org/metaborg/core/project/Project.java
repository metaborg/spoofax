package org.metaborg.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.IProjectConfig;

/**
 * A project.
 */
public class Project implements IProject {
    private final FileObject location;
    private final @Nullable IProjectConfig config;


    public Project(FileObject location, @Nullable IProjectConfig config) {
        this.location = location;
        this.config = config;
    }


    @Override public FileObject location() {
        return location;
    }

    @Override public @Nullable IProjectConfig config() {
        return config;
    }


    @Override public String toString() {
        return location.toString();
    }
}
