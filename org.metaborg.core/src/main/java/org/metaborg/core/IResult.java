package org.metaborg.core;

import javax.annotation.Nullable;

import org.metaborg.core.messages.IMessage;

public interface IResult<T> {
    public abstract @Nullable T value();

    public abstract Iterable<IMessage> messages();

    public abstract long duration();
}
