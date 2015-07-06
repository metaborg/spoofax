package org.metaborg.core.processing;

public class NullProgressReporter implements IProgressReporter {
    @Override public void work(int ticks) {
    }

    @Override public void setWorkRemaining(int ticks) {
    }

    @Override public IProgressReporter subProgress(int ticks) {
        return new NullProgressReporter();
    }
}
