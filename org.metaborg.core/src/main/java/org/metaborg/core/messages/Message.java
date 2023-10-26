package org.metaborg.core.messages;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

public class Message implements IMessage {
    private final String message;
    private final MessageSeverity severity;
    private final MessageType type;
    private final transient @Nullable FileObject source;
    private final @Nullable ISourceRegion region;
    private final transient @Nullable Throwable exception;


    public Message(String message, MessageSeverity severity, MessageType type, @Nullable FileObject source,
        @Nullable ISourceRegion region, @Nullable Throwable exception) {
        this.message = message;
        this.severity = severity;
        this.type = type;
        this.source = source;
        this.region = region;
        this.exception = exception;
    }


    @Override public String message() {
        return message;
    }

    @Override public MessageSeverity severity() {
        return severity;
    }

    @Override public MessageType type() {
        return type;
    }

    @Override public @Nullable FileObject source() {
        return source;
    }

    @Override public @Nullable ISourceRegion region() {
        return region;
    }

    @Override public @Nullable Throwable exception() {
        return exception;
    }


    @Override public String toString() {
        return message;
    }
}
