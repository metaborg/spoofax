package org.metaborg.core.processing;

/**
 * Cancellation token implementation that never cancels.
 */
public class NullCancellationToken implements ICancellationToken {
    @Override public boolean cancelled() {
        return false;
    }

    @Override public void throwIfCancelled() {
    }

    @Override public void cancel() {
    }
}
