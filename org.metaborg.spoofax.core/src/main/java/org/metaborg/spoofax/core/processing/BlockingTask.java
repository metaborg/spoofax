package org.metaborg.spoofax.core.processing;

import rx.functions.Func0;

public class BlockingTask<T> implements ITask<T> {
    private final Func0<T> func;

    private T value;
    private boolean completed;


    public BlockingTask(Func0<T> func) {
        this.func = func;
    }


    @Override public void schedule() {
        if(completed) {
            return;
        }

        value = func.call();
        completed = true;
    }

    @Override public void cancel() {
        // Does nothing
    }

    @Override public void interrupt(int killTimeout) {
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
}
