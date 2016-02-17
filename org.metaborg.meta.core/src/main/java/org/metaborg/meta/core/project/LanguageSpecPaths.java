package org.metaborg.meta.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;

public abstract class LanguageSpecPaths implements ILanguageSpecPaths {
    private final FileObject rootDir;


    public LanguageSpecPaths(FileObject rootDir) {
        this.rootDir = rootDir;
    }

    @Override public FileObject rootFolder() {
        return this.rootDir;
    }

    @Override public abstract FileObject outputFolder();

    /**
     * Resolves a path relative to the root folder.
     *
     * @param relativePath
     *            The relative path.
     * @return The resulting {@link FileObject}.
     */
    protected FileObject resolve(String relativePath) {
        return resolve(null, relativePath);
    }

    /**
     * Resolves a path relative to the specified folder.
     *
     * @param dir
     *            The folder to resolve relative to; or <code>null</code> to use the root folder.
     * @param relativePath
     *            The relative path.
     * @return The resulting {@link FileObject}.
     */
    protected FileObject resolve(FileObject dir, final String relativePath) {
        if(dir == null) {
            dir = rootDir;
        }

        try {
            return dir.resolveFile(relativePath);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
}
