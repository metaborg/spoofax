package org.metaborg.spoofax.core.build.processing;

import javax.annotation.Nullable;

public interface ITask<T> {
    /**
     * Schedule this task.
     */
    public abstract void schedule();

    /**
     * Request cancellation of this task. If it was not yet scheduled, nothing happens.
     */
    public abstract void cancel();

    /**
     * Request cancellation of this task and interrupt it. If it was not yet scheduled, nothing happens. If the task has
     * not been cancelled after {@code stopTimeout} milliseconds, the task it stopped by killing the thread it is
     * running on.
     * 
     * @param killTimeout
     *            Timeout in milliseconds after which the task is killed.
     */
    public abstract void interrupt(int killTimeout);

    /**
     * @return If the task has been completed.
     */
    public abstract boolean completed();

    /**
     * @return If the task has been cancelled.
     */
    public abstract boolean cancelled();

    /**
     * @return Result of the task, or null if it has been cancelled, or not completed yet.
     */
    public abstract @Nullable T result();
}
