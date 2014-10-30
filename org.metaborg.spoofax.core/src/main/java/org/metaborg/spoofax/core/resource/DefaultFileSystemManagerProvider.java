package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.CacheStrategy;
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
            manager.setFilesCache(new DefaultFilesCache());
            manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);

            addDefaultProvider(manager);
            addProviders(manager);

            manager.init();
            return manager;
        } catch(FileSystemException e) {
            throw new RuntimeException("Cannot initialze resource service: " + e.getMessage(), e);
        }
    }

    protected void addDefaultProvider(DefaultFileSystemManager manager) throws FileSystemException {
        final DefaultLocalFileProvider provider = new DefaultLocalFileProvider();
        manager.addProvider("file", provider);
        manager.setDefaultProvider(provider);
    }

    protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        manager.addProvider("res", new ResourceFileProvider());
        manager.addProvider("ram", new RamFileProvider());
    }
}
