package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.ui.IEditorInput;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.eclipse.util.Nullable;

/**
 * Extension of the resource service with Eclipse-specific functionality.
 */
public interface IEclipseResourceService extends IResourceService {
    /**
     * Converts an Eclipse resource into a VFS resource.
     * 
     * @param resource
     *            Eclipse resource to convert.
     * @return VFS resource.
     */
    public abstract FileObject resolve(IResource resource);

    /**
     * Converts an Eclipse editor input into a VFS resource, if possible.
     * 
     * @param input
     *            Eclipse editor input to resolve.
     * @return VFS resource, or null if it could not be converted.
     */
    public abstract @Nullable FileObject resolve(IEditorInput input);

    /**
     * Converts an Eclipse resource delta into a resource change.
     * 
     * @param delta
     *            Eclipse Resource delta to convert.
     * @return Resource change, or null if the Eclipse delta does not indicate a change.
     * @throws SpoofaxRuntimeException
     *             When Eclipse resource delta could not be converted.
     */
    public abstract @Nullable IResourceChange resolve(IResourceDelta delta);

    /**
     * Converts a VFS resource into an Eclipse resource, if possible
     * 
     * @param resource
     *            VFS resource
     * @return Eclipse resource, or null if it could not be converted.
     */
    public abstract @Nullable IResource unresolve(FileObject resource);

    /**
     * Rebases a resource on the local file system, but relative to the Eclipse workspace, into a resource on the
     * Eclipse file system.
     * 
     * @param resource
     *            Resource to rebase.
     * @return Rebased resource.
     */
    public abstract FileObject rebase(FileObject resource);
}
