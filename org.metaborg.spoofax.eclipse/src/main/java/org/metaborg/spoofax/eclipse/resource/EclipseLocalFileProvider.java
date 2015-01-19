package org.metaborg.spoofax.eclipse.resource;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.metaborg.spoofax.core.resource.ILocalFileProvider;

public class EclipseLocalFileProvider implements ILocalFileProvider {
    public static final String scheme = "eclipse";

    @Override public File localFile(FileObject fileObject) {
        final EclipseResourceFileObject eclipseFileObject = (EclipseResourceFileObject) fileObject;

        try {
            final IResource resource = eclipseFileObject.resource();
            final IPath path = resource.getRawLocation();
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
