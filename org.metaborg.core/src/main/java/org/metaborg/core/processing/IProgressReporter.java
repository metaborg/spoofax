package org.metaborg.core.processing;

/**
 * Interface for progress reporting.
 */
public interface IProgressReporter {
    /**
     * Report that {@code ticks} worth of work has been done.
     * 
     * @param ticks
     *            Amount of work done.
     */
    public abstract void work(int ticks);

    /**
     * Set the work remaining to {@code ticks}.
     * 
     * @param ticks
     *            Amount of work remaining.
     */
    public abstract void setWorkRemaining(int ticks);

    /**
     * Create a sub progress reporter, with {@code ticks} worth of work being done from this progress reporter.
     * 
     * @param ticks
     *            Amount of work being done i nthis progress reporter.
     * @return Sub progress reporter.
     */
    public abstract IProgressReporter subProgress(int ticks);
}
