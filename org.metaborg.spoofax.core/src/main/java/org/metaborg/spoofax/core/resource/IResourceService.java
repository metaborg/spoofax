package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileSystemManager;

/**
 * Interface for access to the virtual file system.O
 */
public interface IResourceService {
    /**
     * Returns the VFS file system manager.
     * 
     * @return VFS file system manager.
     */
    public FileSystemManager fileSystemManager();
}
