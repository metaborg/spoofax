package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.vfs2.FileObject;

/**
 * Provides path information for configurations.
 */
public final class PathLookup implements Lookup {
    private final FileObject rootFolder;


    /**
     * Initializes a new instance of the {@link PathLookup} class for the specified project root path.
     *
     * @param rootFolder
     *            The root folder.
     */
    public PathLookup(FileObject rootFolder) {
        this.rootFolder = rootFolder;
    }


    @Override public Object lookup(String s) {
        switch(s) {
            case "root":
                return this.rootFolder.getName().getPath();
            default:
                return null;
        }
    }
}
