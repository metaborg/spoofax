package org.metaborg.core.messages;

import jakarta.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

public class MessageBuilder {
    private String message = "";
    private MessageSeverity severity = MessageSeverity.ERROR;
    private MessageType type = MessageType.INTERNAL;
    private @Nullable FileObject source;
    private @Nullable ISourceRegion region;
    private @Nullable Throwable exception;


    public static MessageBuilder create() {
        return new MessageBuilder();
    }


    public MessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }


    public MessageBuilder withSeverity(MessageSeverity severity) {
        this.severity = severity;
        return this;
    }

    public MessageBuilder asNote() {
        this.severity = MessageSeverity.NOTE;
        return this;
    }

    public MessageBuilder asWarning() {
        this.severity = MessageSeverity.WARNING;
        return this;
    }

    public MessageBuilder asError() {
        this.severity = MessageSeverity.ERROR;
        return this;
    }


    public MessageBuilder withType(MessageType type) {
        this.type = type;
        return this;
    }

    public MessageBuilder asInternal() {
        this.type = MessageType.INTERNAL;
        return this;
    }

    public MessageBuilder asParser() {
        this.type = MessageType.PARSER;
        return this;
    }

    public MessageBuilder asAnalysis() {
        this.type = MessageType.ANALYSIS;
        return this;
    }

    public MessageBuilder asTransform() {
        this.type = MessageType.TRANSFORMATION;
        return this;
    }


    public MessageBuilder withSource(FileObject source) {
        this.source = source;
        return this;
    }


    public MessageBuilder withRegion(ISourceRegion region) {
        this.region = region;
        return this;
    }


    public MessageBuilder withException(Throwable exception) {
        this.exception = exception;
        return this;
    }


    public IMessage build() {
        return new Message(message, severity, type, source, region, exception);
    }
}
