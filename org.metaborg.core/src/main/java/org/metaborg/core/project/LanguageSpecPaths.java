package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;

public abstract class LanguageSpecPaths implements ILanguageSpecPaths {

    private final FileObject root;
    private final ILanguageSpecConfig config;

    public LanguageSpecPaths(FileObject rootFolder, ILanguageSpecConfig config) {
        this.root = rootFolder;
        this.config = config;
    }

    @Override
    public FileObject rootFolder() {
        return this.root;
    }

    @Override
    public abstract FileObject outputFolder();

    @Override
    public abstract FileObject configFile();

    /**
     * Resolves a path relative to the root folder.
     *
     * @param relativePath The relative path.
     * @return The resulting {@link FileObject}.
     */
    protected FileObject resolve(final String relativePath) {
        try {
            return this.root.resolveFile(relativePath);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
}
