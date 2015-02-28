package org.metaborg.spoofax.core.resource;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.vfs2.AllFileSelector;

public class ResourceService implements IResourceService {
    private final FileSystemManager fileSystemManager;
    private final FileSystemOptions fileSystemOptions;
    

    @Inject public ResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader) {
        this.fileSystemManager = fileSystemManager;
        this.fileSystemOptions = new FileSystemOptions();
        
        if(classLoader == null)
            classLoader = this.getClass().getClassLoader();
        ResourceFileSystemConfigBuilder.getInstance().setClassLoader(fileSystemOptions, classLoader);
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
            return fileSystemManager.resolveFile(uri, fileSystemOptions);
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

    @Override public File localFile(FileObject resource) {
        try {
            return resource.getFileSystem().replicateFile(resource, new AllFileSelector());
        } catch (FileSystemException ex) {
            return null;
        }
    }


    @Override public FileObject userStorage() {
        try {
            final FileObject storageDir = root().resolveFile(".cache");
            storageDir.createFolder();
            return storageDir;
        } catch(FileSystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public FileSystemManager manager() {
        return fileSystemManager;
    }
}
