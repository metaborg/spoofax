package org.metaborg.spoofax.eclipse.resource;

import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EclipseResourceService extends ResourceService implements IEclipseResourceService {
    private static final Logger logger = LoggerFactory.getLogger(EclipseResourceService.class);


    @Inject public EclipseResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader, Map<String, ILocalFileProvider> localFileProviders) {
        super(fileSystemManager, classLoader, localFileProviders);

    }


    @Override public FileObject resolve(IResource resource) {
        return resolve("eclipse://" + resource.getFullPath().toString());
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
