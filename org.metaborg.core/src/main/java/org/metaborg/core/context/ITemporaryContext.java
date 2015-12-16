package org.metaborg.core.context;

/**
 * {@link IContext} for temporary use. Is not thread-safe. Has to be closed after usage.
 */
public interface ITemporaryContext extends IContext, AutoCloseable {
    /**
     * Close the context, releasing any state.
     */
    public abstract void close();
}
