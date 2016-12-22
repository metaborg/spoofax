package org.metaborg.core.editor;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

/**
 * Editor registry that always returns an empty iterable.
 */
public class NullEditorRegistry implements IEditorRegistry {
    @Override public Iterable<IEditor> openEditors() {
        return Iterables2.empty();
    }

    @Override public void open(FileObject resource, IProject project) {
    }

    @Override public void open(Iterable<FileObject> resources, IProject project) {
    }
}
