package org.metaborg.core.context;

/**
 * Interface for creating {@link IContext} instances.
 */
public interface IContextFactory {
    /**
     * Creates a new context from given identifier. A fast operation, expensive initialization is performed later.
     * 
     * @param identifier
     *            Identifier to create the context with.
     * @return Created context.
     */
    public abstract IContextInternal create(ContextIdentifier identifier);

    /**
     * Creates a new temporary context from given identifier. A fast operation, expensive initialization is performed in
     * {@link IContextInternal#init()}.
     * 
     * @param context
     *            Inner context to create the temporary context with.
     * @return Created context.
     */
    public abstract ITemporaryContextInternal createTemporary(ContextIdentifier identifier);
}
