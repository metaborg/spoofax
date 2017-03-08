package org.metaborg.core.processing;

/**
 * Interface for figuring out if an operation has been cancelled.
 */
public interface ICancel {
    /**
     * @return If cancellation has been requested.
     */
    boolean cancelled();

    /**
     * @throws InterruptedException
     *             When cancellation has been requested.
     */
    void throwIfCancelled() throws InterruptedException;

    /**
     * Request cancellation.
     */
    void cancel();
}
