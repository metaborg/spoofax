package org.metaborg.spoofax.eclipse.resource;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.metaborg.spoofax.core.resource.IResourceService;

@SuppressWarnings("restriction")
public interface IEclipseResourceService extends IResourceService {
    public abstract FileObject resolve(IResource resource);

    public abstract @Nullable IResource unresolve(FileObject resource);
}
