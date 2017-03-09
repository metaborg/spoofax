package org.metaborg.core.processing;

/**
 * Interface for progress reporting.
 */
public interface IProgress {
    /**
     * Report that {@code ticks} worth of work has been done.
     * 
     * @param ticks
     *            Amount of work done.
     */
    void work(int ticks);

    /**
     * Sets the description of the current task.
     * 
     * @param description
     *            Description of the current task.
     */
    void setDescription(String description);

    /**
     * Set the work remaining to {@code ticks}.
     * 
     * @param ticks
     *            Amount of work remaining.
     */
    void setWorkRemaining(int ticks);

    /**
     * Create a sub-progress reporter, with {@code ticks} worth of work being done from the parent reporter. The
     * returned sub-progress reporter must first call {@link #setWorkRemaining(int)} before calls to {@link #work(int)}.
     * 
     * @param ticks
     *            Amount of work being done in this progress reporter.
     * @return Sub progress reporter.
     */
    IProgress subProgress(int ticks);
}
