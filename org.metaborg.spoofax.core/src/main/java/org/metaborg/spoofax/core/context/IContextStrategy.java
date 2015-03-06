package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

/**
 * Interface for {@link IContext} creation/retrieval strategies.
 */
public interface IContextStrategy {
    /**
     * Returns the context identifier for given resource and language.
     * 
     * @param resource
     *            Resource to get a context identifier for.
     * @param language
     *            Language to get a context identifier for.
     * @return Context identifier.
     * @throws ContextException
     *             When a context identifier cannot be returned.
     */
    public ContextIdentifier get(FileObject resource, ILanguage language) throws ContextException;
}
