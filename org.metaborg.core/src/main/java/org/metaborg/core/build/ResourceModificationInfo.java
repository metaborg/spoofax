package org.metaborg.core.build;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;

class ResourceModificationInfo {
    public final FileObject resource;
    public final long modificationDate;


    public ResourceModificationInfo(FileObject resource) {
        this.resource = resource;
        try {
            this.modificationDate = resource.getContent().getLastModifiedTime();
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException("Could not create resource modification info", e);
        }
    }

    public ResourceModificationInfo(FileObject resource, long modificationDate) {
        this.resource = resource;
        this.modificationDate = modificationDate;
    }
}