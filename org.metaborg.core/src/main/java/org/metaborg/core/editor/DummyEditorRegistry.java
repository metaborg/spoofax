package org.metaborg.core.editor;

import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyEditorRegistry implements IEditorRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DummyEditorRegistry.class);


    @Override public Iterable<IEditor> openEditors() {
        logger.warn("Using dummy editor registry which always returns an empty iterable. "
            + "Bind an actual implementation of IEditorRegistry in your Guice module.");
        return Iterables2.<IEditor>empty();
    }
}
