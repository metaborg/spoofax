package org.metaborg.core.processing;

/**
 * Cancellation token implementation that never cancels.
 */
public class NullCancel implements ICancel {
    @Override public boolean cancelled() {
        return false;
    }

    @Override public void throwIfCancelled() {
    }

    @Override public void cancel() {
    }
}
