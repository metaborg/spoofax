package org.metaborg.spoofax.eclipse.resource;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;

import com.google.inject.Inject;

public class EclipseLocalFileProvider implements ILocalFileProvider {
    public static final String scheme = "eclipse";

    public final IEclipseResourceService resourceService;


    @Inject public EclipseLocalFileProvider(IEclipseResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public File localFile(FileObject resource) {
        try {
            final IResource eclipseResource = resourceService.unresolve(resource);
            if(eclipseResource == null) {
                return null;
            }
            IPath path = eclipseResource.getRawLocation();
            if(path == null) {
                path = eclipseResource.getLocation();
            }
            if(path == null) {
                return null;
            }
            return path.makeAbsolute().toFile();
        } catch(Exception e) {
            return null;
        }
    }

    @Override public String scheme() {
        return scheme;
    }
}
