package org.metaborg.core.editor;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

/**
 * Dummy editor registry that gets bound by default, to display sane warnings when nothing else is bound to
 * {@link IEditorRegistry}. Replace with working implementation or bind {@link NullEditorRegistry} to disable the
 * warning.
 */
public class DummyEditorRegistry implements IEditorRegistry {
    private static final ILogger logger = LoggerUtils.logger(DummyEditorRegistry.class);


    @Override public Iterable<IEditor> openEditors() {
        logger.warn("Using dummy editor registry. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
        return Iterables2.empty();
    }


    @Override public void open(FileObject resource, IProject project) {
        logger.warn("Using dummy editor registry. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
    }

    @Override public void open(Iterable<FileObject> resources, IProject project) {
        logger.warn("Using dummy editor registry. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
    }
}
