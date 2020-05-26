package org.metaborg.core.processing;

import java.util.function.Supplier;

/**
 * Task implementation that executes a function in blocking way when scheduled. Does not support cancellation.
 */
public class BlockingTask<T> implements ITask<T> {
    private final Supplier<T> func;

    private T value;
    private boolean completed;


    public BlockingTask(Supplier<T> func) {
        this.func = func;
    }


    @Override public ITask<T> schedule() {
        if(completed) {
            return this;
        }

        value = func.get();
        completed = true;

        return this;
    }

    @Override public void cancel() {
        // Does nothing
    }

    @Override public void cancel(int killTimeout) {
        // Does nothing
    }

    @Override public boolean completed() {
        return completed;
    }

    @Override public boolean cancelled() {
        return false;
    }

    @Override public T result() {
        return value;
    }

    @Override public ITask<T> block() {
        // Does nothing, schedule already blocks.
        return this;
    }
}
