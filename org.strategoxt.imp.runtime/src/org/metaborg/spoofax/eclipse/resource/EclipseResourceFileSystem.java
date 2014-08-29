package org.metaborg.spoofax.eclipse.resource;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public class EclipseResourceFileSystem extends AbstractFileSystem {
    private final IWorkspaceRoot root;

    public EclipseResourceFileSystem(FileName rootName, FileObject parentLayer, FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
        this.root = ResourcesPlugin.getWorkspace().getRoot();
    }

    @Override protected FileObject createFile(AbstractFileName name) throws Exception {
        return new EclipseResourceFileObject(name, this, root.findMember(name.getFriendlyURI()));
    }

    @Override protected void addCapabilities(Collection<Capability> caps) {
        // TODO Auto-generated method stub

    }
}
