package org.metaborg.core.processing;

public interface ICancellationToken {
    /**
     * @return If cancellation has been requested.
     */
    public abstract boolean cancelled();
}
