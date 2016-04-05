package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

/**
 * Interface for retrieving or creating {@link IContext} instances.
 */
public interface IContextService {
    /**
     * Checks if contexts are available for given language implementation.
     * 
     * @param language
     *            Language implementation to check.
     * @return True if contexts are available, false if not.
     */
    boolean available(ILanguageImpl language);

    /**
     * Retrieves or creates a context for given resource and language.
     *
     * @param resource
     *            Resource to get a context for.
     * @param project
     *            The project the resource belongs to.
     * @param language
     *            Language to get a context for.
     * @return Existing or created context.
     * @throws ContextException
     *             When an error occurs while retrieving or creating a context.
     * @throws MetaborgRuntimeException
     *             When {@code language} does not have a {@link ContextFacet}.
     */
    IContext get(FileObject resource, IProject project, ILanguageImpl language) throws ContextException;

    /**
     * Creates a temporary context for given resource and language. Temporary contexts are not thread-safe, and must be
     * closed after usage. Supports the try-with-resources statement for closing the temporary context.
     *
     * @param resource
     *            Resource to get a context for.
     * @param project
     *            The project the resource belongs to.
     * @param language
     *            Language to get a context for.
     * @return Temporary context.
     * @throws ContextException
     *             When an error occurs while retrieving or creating a context.
     * @throws MetaborgRuntimeException
     *             When {@code language} does not have a {@link ContextFacet}.
     */
    ITemporaryContext getTemporary(FileObject resource, IProject project, ILanguageImpl language)
        throws ContextException;

    /**
     * Unloads given context, persisting it to disk (if supported by the context) and removing it from memory.
     * 
     * @param context
     *            Context to unload.
     */
    void unload(IContext context);
}
