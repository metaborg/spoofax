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
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.util.file.FileUtils;
import org.metaborg.util.file.URIEncode;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ResourceService implements IResourceService {
    private static final ILogger logger = LoggerUtils.logger(ResourceService.class);

    private final FileSystemManager fileSystemManager;
    private final FileSystemOptions fileSystemOptions;


    @Inject public ResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader) {
        this.fileSystemManager = fileSystemManager;
        this.fileSystemOptions = new FileSystemOptions();

        if(classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        ResourceFileSystemConfigBuilder.getInstance().setClassLoader(fileSystemOptions, classLoader);
    }

    @Override public void close() throws Exception {
        if(fileSystemManager instanceof DefaultFileSystemManager) {
            final DefaultFileSystemManager defaultFileSystemManager = (DefaultFileSystemManager) fileSystemManager;
            final FileReplicator replicator = defaultFileSystemManager.getReplicator();
            if(replicator instanceof DefaultFileReplicator) {
                final DefaultFileReplicator defaultFileReplicator = (DefaultFileReplicator) replicator;
                defaultFileReplicator.close();
            } else {
                logger.warn("File replicator {} does not support cleaning up generated temporary files", replicator);
            }
        } else {
            logger.warn("File system manager {} does not support cleaning up generated temporary files",
                fileSystemManager);
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
            return parent.resolveFile(path);
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
                copyLoc = dir.resolveFile(resource.getName().getBaseName());
            }
            copyLoc.copyFrom(resource, new AllFileSelector());

            return localDir;
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
