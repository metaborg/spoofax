package org.metaborg.spoofax.core.resource;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.ftps.FtpsFileProvider;
import org.apache.commons.vfs2.provider.gzip.GzipFileProvider;
import org.apache.commons.vfs2.provider.https.HttpsFileProvider;
import org.apache.commons.vfs2.provider.jar.JarFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.tar.TarFileProvider;
import org.apache.commons.vfs2.provider.tar.Tbz2FileProvider;
import org.apache.commons.vfs2.provider.tar.TgzFileProvider;
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider;
import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider;
import org.apache.commons.vfs2.provider.zip.ZipFileProvider;

import com.google.inject.Provider;

/**
 * Guice provider for the VFS file system manager. Subclass and override the protected methods, and bind the
 * provider in your Guice module, to customize the file providers.
 */
public class DefaultFileSystemManagerProvider implements Provider<FileSystemManager> {
    @Override public FileSystemManager get() {
        try {
            final DefaultFileSystemManager manager = new DefaultFileSystemManager();

            manager.setFilesCache(new DefaultFilesCache());
            manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);

            manager.setTemporaryFileStore(new DefaultFileReplicator());
            manager.setReplicator(new DefaultFileReplicator());

            addDefaultProvider(manager);
            addProviders(manager);
            setBaseFile(manager);

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

    protected void setBaseFile(DefaultFileSystemManager manager) throws FileSystemException {
        manager.setBaseFile(manager.resolveFile(System.getProperty("user.dir")));
    }

    protected void addProviders(DefaultFileSystemManager manager) throws FileSystemException {
        manager.addProvider("tmp", new TemporaryFileProvider());
        manager.addProvider("res", new ResourceFileProvider());
        manager.addProvider("ram", new RamFileProvider());
        manager.addProvider("zip", new ZipFileProvider());
        manager.addProvider("jar", new JarFileProvider());
        manager.addProvider("tar", new TarFileProvider());
        manager.addProvider("tgz", new TgzFileProvider());
        manager.addProvider("tbz2", new Tbz2FileProvider());
        manager.addProvider("gz", new GzipFileProvider());
        manager.addProvider("bz2", new Bzip2FileProvider());
        manager.addProvider("http", new HttpsFileProvider());
        manager.addProvider("https", new HttpsFileProvider());
        manager.addProvider("webdav", new WebdavFileProvider());
        manager.addProvider("ftp", new FtpFileProvider());
        manager.addProvider("ftps", new FtpsFileProvider());
    }
}
