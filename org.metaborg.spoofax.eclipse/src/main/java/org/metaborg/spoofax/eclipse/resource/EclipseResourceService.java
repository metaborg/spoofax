package org.metaborg.spoofax.eclipse.resource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.resource.IResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChange;
import org.metaborg.spoofax.core.resource.ResourceChangeKind;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.eclipse.util.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EclipseResourceService extends ResourceService implements IEclipseResourceService {
    private static final Logger logger = LoggerFactory.getLogger(EclipseResourceService.class);


    @Inject public EclipseResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader) {
        super(fileSystemManager, classLoader);
    }


    @Override public FileObject resolve(IResource resource) {
        return resolve("eclipse://" + resource.getFullPath().toString());
    }

    @Override public @Nullable IResourceChange resolve(IResourceDelta delta) {
        final FileObject resource = resolve(delta.getResource());
        final int eclipseKind = delta.getKind();
        final ResourceChangeKind kind;
        // GTODO: handle move/copies better
        switch(eclipseKind) {
            case IResourceDelta.NO_CHANGE:
                return null;
            case IResourceDelta.ADDED:
                kind = ResourceChangeKind.Create;
                break;
            case IResourceDelta.REMOVED:
                kind = ResourceChangeKind.Delete;
                break;
            case IResourceDelta.CHANGED:
                kind = ResourceChangeKind.Modify;
                break;
            default:
                final String message = String.format("Unhandled resource delta type: %s", eclipseKind);
                logger.error(message);
                throw new SpoofaxRuntimeException(message);
        }

        return new ResourceChange(resource, kind);
    }

    @Override public FileObject resolve(IEditorInput input) {
        if(input instanceof IFileEditorInput) {
            final IFileEditorInput fileInput = (IFileEditorInput) input;
            return resolve(fileInput.getFile());
        }
        logger.error("Could not resolve editor input {}", input);
        return null;
    }

    @Override public IResource unresolve(FileObject resource) {
        if(resource instanceof EclipseResourceFileObject) {
            final EclipseResourceFileObject eclipseResource = (EclipseResourceFileObject) resource;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                logger.error("Could not unresolve resource {} to an Eclipse resource", resource);
                return null;
            }
        }
        // LEGACY: analysis returns messages with local file resources, we need to resolve these to Eclipse
        // resources to show markers.
        // GTODO: support absolute resources
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final String relativePath = resource.getName().getPath();
        return root.findMember(relativePath);
    }

    @Override public FileObject rebase(FileObject resource) {
        return resolve("eclipse://" + resource.getName().getPath());
    }
}
