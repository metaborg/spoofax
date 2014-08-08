package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

public class ResourceService implements IResourceService {
    private final StandardFileSystemManager fileSystemManager;


    public ResourceService() {
        try {
            fileSystemManager = new StandardFileSystemManager();
            fileSystemManager.init();
        } catch(FileSystemException e) {
            throw new RuntimeException("Cannot initialze resource service: " + e.getMessage(), e);
        }
    }


    @Override public FileSystemManager fileSystemManager() {
        return fileSystemManager;
    }
}
