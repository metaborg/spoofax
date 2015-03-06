package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;

/**
 * Interface for retrieving or creating {@link IContext} instances.
 */
public interface IContextService {
    /**
     * Retrieves or creates a context for given resource and language.
     * 
     * @param resource
     *            Resource to get a context for.
     * @param language
     *            Language to get a context for.
     * @return Existing or created context.
     * @throws ContextException
     *             When an error occurs when retrieving or creating a context.
     * @throws SpoofaxRuntimeException
     *             When {@code language} does not have a {@link ContextFacet}.
     */
    public IContext get(FileObject resource, ILanguage language) throws ContextException;

    /**
     * Unloads given context, optionally persisting it to disk and removing it from memory.
     * 
     * @param context
     *            Context to unload.
     */
    public void unload(IContext context);
}
