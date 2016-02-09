package org.metaborg.core.editor;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;

public interface IEditorRegistry {
    /**
     * @return All open editors.
     */
    Iterable<IEditor> openEditors();

    /**
     * Open an editor for given resource.
     *
     * @param project
     *            The project that contains the resource.
     * @param resource
     *            Resource to open editor for.
     */
    void open(ILanguageImpl project, FileObject resource);
}
