package org.metaborg.spoofax.eclipse.resource;

import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;
import org.metaborg.spoofax.core.resource.ResourceService;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class EclipseResourceService extends ResourceService implements IEclipseResourceService {
    @Inject public EclipseResourceService(FileSystemManager fileSystemManager,
        @Named("ResourceClassLoader") ClassLoader classLoader, Map<String, ILocalFileProvider> localFileProviders) {
        super(fileSystemManager, classLoader, localFileProviders);

    }


    public FileObject resolve(IResource resource) {
        return resolve("eclipse://" + resource.getFullPath().toString());
    }


    @Override public IResource unresolve(FileObject resource) {
        if(resource instanceof EclipseResourceFileObject) {
            final EclipseResourceFileObject eclipseResource = (EclipseResourceFileObject) resource;
            try {
                return eclipseResource.resource();
            } catch(Exception e) {
                return null;
            }
        } else {
            // LEGACY: analysis returns messages with local file resources, we need to resolve these to Eclipse
            // resources to show markers.
            // GTODO: support absolute resources
            final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            final String relativePath = resource.getName().getPath();
            return root.findMember(relativePath);
        }
    }
}
