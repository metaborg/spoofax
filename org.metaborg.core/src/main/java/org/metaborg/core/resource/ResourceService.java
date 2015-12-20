package org.metaborg.core.resource;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.util.file.FileUtils;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ResourceService implements IResourceService {
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


    @Override public FileObject root() {
        try {
            return fileSystemManager.getBaseFile();
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public FileObject resolve(String uri) {
        try {
            return fileSystemManager.resolveFile(uri, fileSystemOptions);
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
        final File file = new File(path);

        try {
            final URI uri = new URI(path);
            if(uri.isAbsolute()) {
                return resolve(path);
            }
        } catch(URISyntaxException e) {
            // Ignore
        }

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
            return fileSystemManager.resolveURI(uri);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public File localFile(FileObject resource) {
        try {
            return resource.getFileSystem().replicateFile(resource, new AllFileSelector());
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }

    @Override public File localPath(FileObject resource) {
        if(resource instanceof LocalFile) {
            return FileUtils.toFile(resource);
        }
        return null;
    }


    @Deprecated @Override public FileObject userStorage() {
        try {
            final FileObject storageDir = root().resolveFile(".cache");
            storageDir.createFolder();
            return storageDir;
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }


    public static void writeFileObject(FileObject fo, ObjectOutput out) throws IOException {
        out.writeObject(fo.getName().getURI());
    }

    public static FileObject readFileObject(ObjectInput in) throws ClassNotFoundException, IOException {
        String uri = (String) in.readObject();
        return VFS.getManager().resolveFile(uri);
    }
}
