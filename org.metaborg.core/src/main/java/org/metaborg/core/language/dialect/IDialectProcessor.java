package org.metaborg.core.language.dialect;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.LanguageImplChange;
import org.metaborg.core.resource.ResourceChange;

/**
 * Interface for processing dialect updates.
 */
public interface IDialectProcessor {
    /**
     * Updates dialects using given changes.
     * 
     * @param project
     *            Location to process changes for.
     * @param changes
     *            Resource changes to process.
     */
    public abstract void update(FileObject location, Iterable<ResourceChange> changes);

    /**
     * Updates dialects using a language implementation change.
     * 
     * @param change
     *            Language change to process.
     */
    public abstract void update(LanguageImplChange change);
}
