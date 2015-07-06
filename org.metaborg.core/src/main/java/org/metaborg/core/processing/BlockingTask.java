package org.metaborg.core.processing;

import rx.functions.Func0;

public class BlockingTask<T> implements ITask<T> {
    private final Func0<T> func;

    private T value;
    private boolean completed;


    public BlockingTask(Func0<T> func) {
        this.func = func;
    }


    @Override public ITask<T> schedule() {
        if(completed) {
            return this;
        }

        value = func.call();
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

    @Override public T result() {
        return value;
    }

    @Override public void block() {
        // Does nothing, schedule already blocks.
    }
}
