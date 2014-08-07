package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

public class ResourceService implements IResourceService {
    private final FileSystemManager fileSystemManager;


    public ResourceService() {
        try {
            this.fileSystemManager = VFS.getManager();
        } catch(FileSystemException e) {
            throw new RuntimeException("Cannot initialze resource service: " + e.getMessage(), e);
        }
    }


    @Override public FileSystemManager fileSystemManager() {
        return fileSystemManager;
    }
}
