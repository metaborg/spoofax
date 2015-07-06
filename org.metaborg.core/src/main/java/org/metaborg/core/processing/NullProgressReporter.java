package org.metaborg.core.processing;

/**
 * Progress reporter implementation that ignores all progress reporting.
 */
public class NullProgressReporter implements IProgressReporter {
    @Override public void work(int ticks) {
    }

    @Override public void setWorkRemaining(int ticks) {
    }

    @Override public IProgressReporter subProgress(int ticks) {
        return new NullProgressReporter();
    }
}
