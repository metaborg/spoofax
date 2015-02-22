package org.metaborg.spoofax.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public class Message implements IMessage {
    private final String message;
    private final MessageSeverity severity;
    private final MessageType type;
    private final FileObject source;
    private final ISourceRegion region;
    @Nullable private final Throwable exception;


    public Message(String message, MessageSeverity severity, MessageType type, FileObject source,
        ISourceRegion region, @Nullable Throwable exception) {
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

    @Override public FileObject source() {
        return source;
    }

    @Override public ISourceRegion region() {
        return region;
    }

    @Override public Throwable exception() {
        return exception;
    }

    @Override public String toString() {
        return message;
    }
}
