package org.metaborg.core.project;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.configuration.ILanguageSpecConfig;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.file.FileUtils;

import java.net.URI;

public abstract class LanguageSpecPaths implements ILanguageSpecPaths {

    private final ILanguageSpecConfig config;
    private transient FileObject root;

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

    /**
     * Resolves a path relative to the root folder.
     *
     * @param relativePath The relative path.
     * @return The resulting {@link FileObject}.
     */
    protected FileObject resolve(final String relativePath) {
        return resolve(null, relativePath);
    }

    /**
     * Resolves a path relative to the specified folder.
     *
     * @param folder The folder to resolve relative to;
     *               or <code>null</code> to use the root folder.
     * @param relativePath The relative path.
     * @return The resulting {@link FileObject}.
     */
    protected FileObject resolve(FileObject folder, final String relativePath) {
        if (folder == null)
            folder = this.root;
        try {
            return folder.resolveFile(relativePath);
        } catch(FileSystemException e) {
            throw new MetaborgRuntimeException(e);
        }
    }
}
