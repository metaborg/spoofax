package org.metaborg.spoofax.eclipse.resource;

import java.io.InputStream;

import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

public class EclipseResourceFileObject extends AbstractFileObject {
    private final IResource resource;

    public EclipseResourceFileObject(AbstractFileName name, AbstractFileSystem fs, IResource resource) {
        super(name, fs);

        this.resource = resource;
    }

    @Override protected FileType doGetType() throws Exception {
        switch(resource.getType()) {
            case IResource.FILE:
                return FileType.FILE;
            case IResource.FOLDER:
            case IResource.PROJECT:
            case IResource.ROOT:
                return FileType.FOLDER;
        }

        return FileType.IMAGINARY;
    }

    @Override protected String[] doListChildren() throws Exception {
        if(resource.getType() == IResource.FOLDER) {
            final IFolder folder = (IFolder) resource;
            final IResource[] members = folder.members();
            final String[] memberNames = new String[members.length];
            for(int i = 0; i < members.length; ++i) {
                memberNames[i] = members[i].getFullPath().toPortableString();
            }
            return memberNames;
        } else {
            throw new IllegalStateException("Resource is not a folder, cannot retrieve children.");
        }
    }

    @Override protected long doGetContentSize() throws Exception {
        final IFileStore store = EFS.getStore(resource.getLocationURI());
        final IFileInfo info = store.fetchInfo();
        return info.getLength();
    }

    @Override protected InputStream doGetInputStream() throws Exception {
        if(resource.getType() == IResource.FILE) {
            final IFile file = (IFile) resource;
            return file.getContents();
        } else {
            throw new IllegalStateException("Resource is not a file, cannot create an input stream.");
        }
    }
}
