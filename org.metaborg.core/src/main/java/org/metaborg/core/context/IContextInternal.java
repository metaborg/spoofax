package org.metaborg.core.context;

/**
 * Extension of {@link IContext} with methods that should not be exposed to clients.
 */
public interface IContextInternal extends IContext {
    /**
     * @return Identifier of this context.
     */
    public abstract ContextIdentifier identifier();

    /**
     * Initializes the context. Any expensive operations are done in this method, instead of the constructor.
     */
    public abstract void initialize();

    /**
     * Unloads the context, optionally persisting it to disk and removing it from memory. Clients should not call this
     * method.
     */
    public abstract void unload();
}
