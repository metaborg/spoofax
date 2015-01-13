package org.metaborg.spoofax.eclipse.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystemException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.metaborg.util.stream.OnCloseByteArrayOutputStream;
import org.spoofax.terms.util.NotImplementedException;

import rx.functions.Action1;

public class EclipseResourceFileObject extends AbstractFileObject {
    private final AbstractFileName name;
    private final IWorkspaceRoot root;

    private boolean attached = false;
    private IResource resource;
    private IFileInfo info;


    public EclipseResourceFileObject(AbstractFileName name, IWorkspaceRoot root, AbstractFileSystem fs) {
        super(name, fs);
        this.name = name;
        this.root = root;
    }


    private void update() throws Exception {
        updateResource();
        updateFileInfo();
    }

    private void updateResource() throws Exception {
        resource = root.findMember(name.getPath());
    }

    private void updateFileInfo() throws Exception {
        if(resource != null) {
            final IFileStore store = EFS.getStore(resource.getLocationURI());
            info = store.fetchInfo();
        }
    }

    private IPath getPath() {
        return root.getFullPath().append(name.getPath());
    }

    @Override protected void doAttach() throws Exception {
        if(attached)
            return;
        update();
        attached = true;
    }

    @Override protected void onChange() throws Exception {
        update();
    }

    @Override protected void doDetach() throws Exception {
        info = null;
        resource = null;
        attached = false;
    }

    @Override protected FileType doGetType() throws Exception {
        if(resource == null || !resource.exists())
            return FileType.IMAGINARY;

        switch(resource.getType()) {
            case IResource.FILE:
                return FileType.FILE;
            case IResource.FOLDER:
            case IResource.PROJECT:
            case IResource.ROOT:
                return FileType.FOLDER;
            default:
                return FileType.IMAGINARY;
        }
    }

    @Override protected boolean doIsHidden() throws Exception {
        return info.getAttribute(EFS.ATTRIBUTE_HIDDEN);
    }

    @Override protected boolean doIsReadable() throws Exception {
        return info.getAttribute(EFS.ATTRIBUTE_OWNER_READ) || info.getAttribute(EFS.ATTRIBUTE_GROUP_READ)
            || info.getAttribute(EFS.ATTRIBUTE_OTHER_READ);
    }

    @Override protected boolean doIsWriteable() throws Exception {
        return info.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE) || info.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE)
            || info.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE);
    }

    @Override protected String[] doListChildren() throws Exception {
        final IContainer container = (IContainer) resource;
        final IResource[] members = container.members();
        final String[] memberNames = new String[members.length];
        for(int i = 0; i < members.length; ++i) {
            memberNames[i] = members[i].getFullPath().toPortableString();
        }
        return memberNames;
    }

    @Override protected FileObject[] doListChildrenResolved() throws Exception {
        final String[] children = doListChildren();
        final FileSystem fileSystem = getFileSystem();
        final FileObject[] files = new FileObject[children.length];
        for(int i = 0; i < children.length; ++i) {
            files[i] = fileSystem.resolveFile(children[i]);
        }
        return files;
    }

    @Override protected long doGetContentSize() throws Exception {
        return info.getLength();
    }

    @Override protected void doDelete() throws Exception {
        resource.delete(true, null);
    }

    @Override protected void doRename(FileObject newfile) throws Exception {
        throw new NotImplementedException();
    }

    @Override protected void doCreateFolder() throws Exception {
        final IPath path = getPath();
        if(path.segmentCount() == 1) {
            final IProject project = root.getProject(path.segment(0));
            project.create(null);
            project.open(null);
        } else {
            getParent().createFolder();
            final IFolder folder = root.getFolder(path);
            folder.create(true, true, null);
        }
    }

    @Override protected OutputStream doGetOutputStream(boolean bAppend) throws Exception {
        final IFile file;
        if(resource == null) {
            final IPath path = getPath();
            if(path.segmentCount() == 1) {
                throw new FileSystemException("Cannot create a file under the workspace root.");
            }
            getParent().createFolder();
            file = root.getFile(path);
            file.create(null, true, null);
        } else {
            file = (IFile) resource;
        }

        return new OnCloseByteArrayOutputStream(new Action1<ByteArrayOutputStream>() {
            @Override public void call(ByteArrayOutputStream out) {
                try {
                    file.setContents(new ByteArrayInputStream(out.toByteArray()), true, false, null);
                } catch(CoreException e) {
                    throw new RuntimeException("Could not set file contents for file " + name, e);
                }
            }
        });
    }

    @Override protected InputStream doGetInputStream() throws Exception {
        final IFile file = (IFile) resource;
        return file.getContents();
    }
}
