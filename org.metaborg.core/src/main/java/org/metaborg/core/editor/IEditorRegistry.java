package org.metaborg.core.editor;

import org.apache.commons.vfs2.FileObject;

public interface IEditorRegistry {
    /**
     * @return All open editors.
     */
    public abstract Iterable<IEditor> openEditors();

    /**
     * Open an editor for given resource.
     * 
     * @param resource
     *            Resource to open editor for.
     */
    public abstract void open(FileObject resource);
}
