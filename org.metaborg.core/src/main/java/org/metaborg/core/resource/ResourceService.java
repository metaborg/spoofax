package org.metaborg.core.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;
import org.metaborg.core.MetaborgRuntimeException;
import mb.util.vfs2.file.FileUtils;
import mb.util.vfs2.file.URIEncode;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import mb.util.vfs2.resource.ResourceUtils;


public class ResourceService implements IResourceService {
    private static final ILogger logger = LoggerUtils.logger(ResourceService.class);

    private final FileSystemManager fileSystemManager;
    private final FileSystemOptions fileSystemOptions;


    @jakarta.inject.Inject @javax.inject.Inject public ResourceService(FileSystemManager fileSystemManager,
        @jakarta.inject.Named("ResourceClassLoader") @javax.inject.Named("ResourceClassLoader") ClassLoader classLoader) {
        this.fileSystemManager = fileSystemManager;
        this.fileSystemOptions = new FileSystemOptions();

        if(classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        ResourceFileSystemConfigBuilder.getInstance().setClassLoader(fileSystemOptions, classLoader);
    }

    @Override public void close() {
        if(fileSystemManager instanceof DefaultFileSystemManager) {
            final DefaultFileSystemManager defaultFileSystemManager = (DefaultFileSystemManager) fileSystemManager;
            defaultFileSystemManager.close();
        } else {
            logger.warn("File system manager {} does not support cleaning up", fileSystemManager);
        }
    }


    @Override public FileObject root() {
        try {
            return fileSystemManager.getBaseFile();
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileObject resolve(String uri) {
        try {
            final String uriEncoded = URIEncode.encode(uri);
            return fileSystemManager.resolveFile(uriEncoded, fileSystemOptions);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileObject resolve(File file) {
        try {
            return fileSystemManager.toFileObject(file);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileObject resolve(URI uri) {
        try {
            return fileSystemManager.resolveFile(uri.toString());
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileObject resolve(FileObject parent, String path) {
        try {
            final String pathEncoded = URIEncode.encode(path);
            final URI uri = new URI(pathEncoded);
            if(uri.isAbsolute()) {
                return resolve(uri);
            }
        } catch(URISyntaxException e) {
            // Ignore
        }

        final File file = new File(path);
        if(file.isAbsolute()) {
            return resolve("file://" + path);
        }

        try {
            return ResourceUtils.resolveFile(parent, path);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileName resolveToName(String uri) {
        try {
            final String uriEncoded = URIEncode.encode(uri);
            return fileSystemManager.resolveURI(uriEncoded);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileName resolveToName(URI uri) {
        try {
            return fileSystemManager.resolveURI(uri.toString());
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public File localFile(FileObject resource) {
        if(resource instanceof LocalFile) {
            return FileUtils.toFile(resource);
        }

        try {
            return resource.getFileSystem().replicateFile(resource, new AllFileSelector());
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException("Could not get local file for " + resource, e);
        }
    }

    @Override public File localFile(FileObject resource, FileObject dir) {
        if(resource instanceof LocalFile) {
            return FileUtils.toFile(resource);
        }

        final File localDir = localPath(dir);
        if(localDir == null) {
            throw new MetaborgRuntimeException("Replication directory " + dir
                + " is not on the local filesystem, cannot get local file for " + resource);
        }
        try {
            dir.createFolder();

            final FileObject copyLoc;
            if(resource.getType() == FileType.FOLDER) {
                copyLoc = dir;
            } else {
                copyLoc = ResourceUtils.resolveFile(dir, resource.getName().getBaseName());
            }
            copyLoc.copyFrom(resource, new AllFileSelector());

            return localPath(copyLoc);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException("Could not get local file for " + resource, e);
        }
    }

    @Override public File localFileUpdate(FileObject resource, FileObject dir) {
        if(resource instanceof LocalFile) {
            return FileUtils.toFile(resource);
        }

        final File localDir = localPath(dir);
        if(localDir == null) {
            throw new MetaborgRuntimeException("Replication directory " + dir
                + " is not on the local filesystem, cannot get local file for " + resource);
        }
        try {
            final FileObject copyLoc;
            if(resource.getType() == FileType.FOLDER) {
                copyLoc = dir;
            } else {
                copyLoc = ResourceUtils.resolveFile(dir, resource.getName().getBaseName());
            }
            copyLoc.copyFrom(resource, new ModifiedFileSelector(copyLoc));

            return localPath(copyLoc);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException("Could not get local file for " + resource, e);
        }
    }

    @Override public File localPath(FileObject resource) {
        if(resource instanceof LocalFile) {
            return FileUtils.toFile(resource);
        }
        return null;
    }
}
