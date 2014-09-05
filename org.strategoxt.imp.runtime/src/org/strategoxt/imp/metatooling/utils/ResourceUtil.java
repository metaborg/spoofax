/**
 * 
 */
package org.strategoxt.imp.metatooling.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.strategoxt.imp.runtime.Environment;

/**
 * Simple utilities for locating IResources based on actual paths.
 */
public class ResourceUtil {
    public static IResource getResource(String file) {
        File fileRef = new File(file);
        try {
            fileRef = fileRef.getCanonicalFile();
        } catch(IOException e) {
            Environment.logException(e);
        }
        URI uri = fileRef.toURI();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IResource[] resources = workspace.getRoot().findFilesForLocationURI(uri);
        if(resources.length == 0)
            throw new IllegalArgumentException("File not in workspace: " + file);

        IResource resource = resources[0];
        return resource;
    }
}
