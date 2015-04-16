package org.metaborg.spoofax.eclipse.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class EclipseResourceFileSystem extends AbstractFileSystem {
    private final IWorkspaceRoot root;


    public EclipseResourceFileSystem(FileName rootName, FileObject parentLayer, FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }


    @Override protected FileObject createFile(AbstractFileName name) throws Exception {
        return new EclipseResourceFileObject(name, root, this);
    }

    @Override protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(EclipseResourceProvider.capabilities);
    }

    @Override protected File doReplicateFile(FileObject file, FileSelector selector) throws Exception {
        final EclipseResourceFileObject eclipseResource = (EclipseResourceFileObject) file;
        final IResource resource = eclipseResource.resource();
        if(resource == null) {
            throw new IOException("Cannot get Eclipse resource corresponding to " + file);
        }
        IPath path = resource.getRawLocation();
        if(path == null) {
            path = resource.getLocation();
        }
        if(path == null) {
            throw new IOException("Resource " + file + " does not reside on the local file system");
        }
        return path.makeAbsolute().toFile();
    }
}
