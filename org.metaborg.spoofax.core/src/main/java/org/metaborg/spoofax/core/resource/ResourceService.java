package org.metaborg.spoofax.core.resource;

import java.io.File;
import java.util.Collection;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class ResourceService implements IResourceService {
    private final FileSystemManager fileSystemManager;


    @Inject public ResourceService(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }


    @Override public FileObject root() {
        try {
            return fileSystemManager.getBaseFile();
        } catch(FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public FileObject resolve(String uri) {
        try {
            return fileSystemManager.resolveFile(uri);
        } catch(FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public FileObject resolve(File file) {
        try {
            return fileSystemManager.toFileObject(file);
        } catch(FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Iterable<FileObject> resolveAll(Iterable<String> uris) {
        final Collection<FileObject> files = Lists.newLinkedList();
        for(String uri : uris) {
            files.add(resolve(uri));
        }
        return files;
    }

    @Override public FileName resolveURI(String uri) {
        try {
            return fileSystemManager.resolveURI(uri);
        } catch(FileSystemException e) {
            throw new RuntimeException(e);
        }
    }
}
