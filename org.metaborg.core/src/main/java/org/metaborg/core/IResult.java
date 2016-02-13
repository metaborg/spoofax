package org.metaborg.core;

import javax.annotation.Nullable;

import org.metaborg.core.messages.IMessage;

public interface IResult<T> {
    @Nullable T value();

    Iterable<IMessage> messages();

    long duration();
}
