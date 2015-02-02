package org.metaborg.spoofax.eclipse.util;

import java.io.IOException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public final class ResourceUtils {
    public static IResource getProjectDirectory(IResource resource) throws IOException {
        final IProject project = resource.getProject();
        if(project != null) {
            return project;
        }

        final IContainer parent = resource.getParent();
        if(parent != null) {
            return parent;
        }

        throw new IOException("Could not get project directory for resource " + resource.toString());
    }
}
