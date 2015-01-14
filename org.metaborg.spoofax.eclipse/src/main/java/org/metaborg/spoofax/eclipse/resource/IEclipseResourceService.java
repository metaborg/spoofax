package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.metaborg.spoofax.core.resource.IResourceService;

public interface IEclipseResourceService extends IResourceService {
    public abstract FileObject resolve(IResource resource);
}
