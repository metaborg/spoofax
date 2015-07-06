package org.metaborg.core.processing;

import javax.annotation.Nullable;

/**
 * Interface for asynchronous task with cancellation and progress reporting.
 * 
 * @param <T>
 */
public interface ITask<T> {
    /**
     * Schedule this task and returns itself.
     */
    public abstract ITask<T> schedule();

    /**
     * Request cancellation.
     */
    public abstract void cancel();

    /**
     * Request cancellation, force cancel after after {@code stopTimeout} milliseconds.
     * 
     * @param forceTimeout
     *            Timeout in milliseconds after which the cancellation is forced.
     */
    public abstract void cancel(int forceTimeout);

    /**
     * @return If the task has been completed.
     */
    public abstract boolean completed();

    /**
     * @return Result of the task, or null if it has been cancelled, or not completed yet.
     */
    public abstract @Nullable T result();

    /**
     * Blocks until the task has been killed or completed.
     */
    public abstract void block() throws InterruptedException;
}
