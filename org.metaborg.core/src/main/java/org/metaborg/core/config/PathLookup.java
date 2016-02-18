package org.metaborg.core.config;

import org.apache.commons.configuration2.interpol.Lookup;
import org.apache.commons.vfs2.FileObject;

/**
 * Provides path information for configurations.
 */
public final class PathLookup implements Lookup {
    private final FileObject rootFolder;


    public PathLookup(FileObject rootFolder) {
        this.rootFolder = rootFolder;
    }


    @Override public Object lookup(String s) {
        switch(s) {
            case "root":
                return rootFolder.getName().getPath();
            default:
                return null;
        }
    }
}
