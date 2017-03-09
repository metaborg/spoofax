package org.metaborg.core.processing;

/**
 * Progress reporter implementation that ignores all progress reporting.
 */
public class NullProgress implements IProgress {
    @Override public void work(int ticks) {
    }

    @Override public void setDescription(String description) {
    }

    @Override public void setWorkRemaining(int ticks) {
    }

    @Override public IProgress subProgress(int ticks) {
        return new NullProgress();
    }
}
