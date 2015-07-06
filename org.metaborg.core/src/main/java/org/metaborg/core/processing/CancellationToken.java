package org.metaborg.core.processing;

/**
 * Simple cancellation token implementation.
 */
public class CancellationToken implements ICancellationToken {
    private volatile boolean cancelled = false;


    @Override public boolean cancelled() {
        return cancelled;
    }


    public void cancel() {
        cancelled = true;
    }
}
