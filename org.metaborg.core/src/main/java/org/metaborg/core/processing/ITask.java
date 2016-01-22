package org.metaborg.core.processing;

import javax.annotation.Nullable;

/**
 * Interface for asynchronous task with cancellation.
 * 
 * @param <T>
 *            Type of the result.
 */
public interface ITask<T> {
    /**
     * Schedule this task and returns itself.
     */
    ITask<T> schedule();

    /**
     * Request cancellation.
     */
    void cancel();

    /**
     * Request cancellation, force cancel after after {@code stopTimeout} milliseconds.
     * 
     * @param forceTimeout
     *            Timeout in milliseconds after which the cancellation is forced.
     */
    void cancel(int forceTimeout);

    /**
     * @return If the task has been completed.
     */
    boolean completed();

    /**
     * @return If the task has been cancelled.
     */
    boolean cancelled();

    /**
     * @return Result of the task, or null if it has been cancelled, or not completed yet.
     */
    @Nullable T result();

    /**
     * Blocks until the task has been completed or cancelled, and return itself.
     * 
     * @throws InterruptedException
     *             When the task has been cancelled while blocking.
     */
    ITask<T> block() throws InterruptedException;
}
