package org.metaborg.spoofax.core.context;

/**
 * Interface for creating {@link IContext} instances.
 */
public interface IContextFactory {
    /**
     * Creates a new context from given identifier. A fast operation, expensive initialization is performed in
     * {@link IContextInternal#initialize()}.
     * 
     * @param identifier
     *            Identifier to create the context with.
     * @return Created context.
     */
    public abstract IContextInternal create(ContextIdentifier identifier);
}
