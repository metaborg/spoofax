package org.metaborg.core.language.dialect;

import org.metaborg.core.language.LanguageChange;
import org.metaborg.core.project.IProject;
import org.metaborg.core.resource.ResourceChange;

/**
 * Interface for processing dialect updates.
 */
public interface IDialectProcessor {
    /**
     * Updates dialects using given changes.
     * 
     * @param project
     *            Project to process changes for.
     * @param changes
     *            Resource changes to process.
     */
    public abstract void update(IProject project, Iterable<ResourceChange> changes);

    /**
     * Updates dialects using a language change.
     * 
     * @param change
     *            Language change to process.
     */
    public abstract void update(LanguageChange change);
}
