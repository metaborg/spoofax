package org.metaborg.core.editor;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy editor registry that gets bound by default, to display sane warnings when nothing else is bound to
 * {@link IEditorRegistry}. Replace with working implementation or bind {@link NullEditorRegistry} to disable the
 * warning.
 */
public class DummyEditorRegistry implements IEditorRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DummyEditorRegistry.class);


    @Override public Iterable<IEditor> openEditors() {
        logger.warn("Using dummy editor registry. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
        return Iterables2.<IEditor>empty();
    }


    @Override public void open(FileObject resource) {
        logger.warn("Using dummy editor registry. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
    }
}
