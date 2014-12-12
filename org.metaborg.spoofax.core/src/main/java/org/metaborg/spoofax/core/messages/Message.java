package org.metaborg.spoofax.core.messages;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public class Message implements IMessage {
    private final String message;
    private final MessageSeverity severity;
    private final MessageType type;
    @Nullable private final FileObject source;
    @Nullable private final String sourceText;
    private final ICodeRegion region;
    @Nullable private final Throwable exception;


    public Message(String message, MessageSeverity severity, MessageType type, @Nullable FileObject source,
        @Nullable String sourceText, ICodeRegion region, @Nullable Throwable exception) {
        this.message = message;
        this.severity = severity;
        this.type = type;
        this.source = source;
        this.sourceText = sourceText;
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

    @Override public String sourceText() {
        return sourceText;
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
        sb.append(CodeRegionHelper.damagedRegion(region, sourceText, "\t"));
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
