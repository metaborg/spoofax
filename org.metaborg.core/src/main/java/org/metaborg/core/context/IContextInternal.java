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
     * Initializes the context if it has not been initialized yet. Will not load anything from disk.Acquires a write
     * lock. Cannot be called while holding the read lock.
     */
    public abstract void init();

    /**
     * Initializes and loads the context if it has not been initialized or loaded yet.Acquires a write lock. Cannot be
     * called while holding the read lock.
     */
    public abstract void load();

    /**
     * Unloads the context, removing it from memory. Acquires a write lock. Cannot be called while holding the read
     * lock.
     */
    public abstract void unload();
}
