package org.metaborg.spoofax.core.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.vfs2.FileObject;

public class Message implements IMessage {
    private final String message;
    private final MessageSeverity severity;
    private final MessageType type;
    private final FileObject source;
    private final ICodeRegion region;
    private final Throwable exception;


    public Message(String message, MessageSeverity severity, MessageType type, FileObject source,
        ICodeRegion region, Throwable exception) {
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

    @Override public ICodeRegion region() {
        return region;
    }

    @Override public Throwable exception() {
        return exception;
    }


    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(severity);
        if(source != null) {
            sb.append(" in ");
            sb.append(source.getName().getPath());
            sb.append(" (at line " + region.startRow() + ")\n");
        } else {
            sb.append(" at line " + region.startRow() + "\n");
        }
        sb.append(region.damagedRegion("\t"));
        sb.append(message);
        sb.append("\n");
        if(exception != null) {
            sb.append("\tCaused by:\n");
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            sb.append(sw.toString());
        }
        sb.append("----------");
        return sb.toString();
    }
}
