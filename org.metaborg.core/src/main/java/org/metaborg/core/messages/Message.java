package org.metaborg.core.messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.source.ISourceRegion;

public class Message implements IMessage {
    private static final long serialVersionUID = -8129122671657252297L;

    private final String message;
    private final MessageSeverity severity;
    private final MessageType type;
    private transient FileObject source;
    private final ISourceRegion region;
    @Nullable private Throwable exception;


    public Message(String message, MessageSeverity severity, MessageType type, FileObject source, ISourceRegion region,
        @Nullable Throwable exception) {
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ResourceService.writeFileObject(source, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        source = ResourceService.readFileObject(in);
    }
}
