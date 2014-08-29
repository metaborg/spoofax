package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;

import com.google.inject.Provider;

/**
 * Guice provider for the VFS file system manager. Subclass and override the protected methods, and bind the provider in
 * your Guice module, to customize the file providers.
 */
public class DefaultFileSystemManagerProvider implements Provider<FileSystemManager> {
    @Override public FileSystemManager get() {
        try {
            final DefaultFileSystemManager manager = new DefaultFileSystemManager();
            addDefaultProvider(manager);
            addProviders(manager);
            manager.setFilesCache(new DefaultFilesCache());
            manager.init();
            return manager;
        } catch(FileSystemException e) {
            throw new RuntimeException("Cannot initialze resource service: " + e.getMessage(), e);
        }
    }

    protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final DefaultLocalFileProvider localProvider = new DefaultLocalFileProvider();
        manager.addProvider("file", localProvider);
        manager.setDefaultProvider(localProvider);
    }

    protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        manager.addProvider("ram", new RamFileProvider());
        manager.addProvider("res", new ResourceFileProvider());
    }
}
