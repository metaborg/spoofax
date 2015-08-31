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
     * Unloads the context, removing it from memory. Acquires a write lock. Cannot be called while holding the read
     * lock.
     */
    public abstract void unload();
}
