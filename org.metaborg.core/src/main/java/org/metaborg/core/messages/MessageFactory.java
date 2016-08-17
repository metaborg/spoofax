package org.metaborg.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

public class MessageFactory {
    public static Message newMessage(@Nullable FileObject resource, ISourceRegion region, String msg, MessageSeverity severity,
        MessageType type, @Nullable Throwable cause) {
        return new Message(msg, severity, type, resource, region, cause);
    }


    public static Message newParseMessage(@Nullable FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity, @Nullable Throwable cause) {
        return newMessage(resource, region, msg, severity, MessageType.PARSER, cause);
    }

    public static Message
        newParseError(@Nullable FileObject resource, ISourceRegion region, String msg, @Nullable Throwable cause) {
        return newParseMessage(resource, region, msg, MessageSeverity.ERROR, cause);
    }

    public static Message newParseWarning(@Nullable FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newParseMessage(resource, region, msg, MessageSeverity.WARNING, cause);
    }


    public static Message newAnalysisMessage(@Nullable FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity, @Nullable Throwable cause) {
        return newMessage(resource, region, msg, severity, MessageType.ANALYSIS, cause);
    }

    public static Message newAnalysisError(@Nullable FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.ERROR, cause);
    }

    public static Message newAnalysisWarning(@Nullable FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.WARNING, cause);
    }

    public static Message newAnalysisNote(@Nullable FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.NOTE, cause);
    }


    public static Message newAtTop(@Nullable FileObject resource, String msg, MessageType type, MessageSeverity severity,
        @Nullable Throwable cause) {
        return new Message(msg, severity, type, resource, null, cause);
    }

    public static Message newErrorAtTop(@Nullable FileObject resource, String msg, MessageType type, @Nullable Throwable cause) {
        return newAtTop(resource, msg, type, MessageSeverity.ERROR, cause);
    }

    public static Message newWarningAtTop(@Nullable FileObject resource, String msg, MessageType type, @Nullable Throwable cause) {
        return newAtTop(resource, msg, type, MessageSeverity.WARNING, cause);
    }

    public static Message newNoteAtTop(@Nullable FileObject resource, String msg, MessageType type, @Nullable Throwable cause) {
        return newAtTop(resource, msg, type, MessageSeverity.NOTE, cause);
    }


    public static Message newParseErrorAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.PARSER, cause);
    }

    public static Message newParseWarningAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.PARSER, cause);
    }


    public static Message newAnalysisMessageAtTop(@Nullable FileObject resource, String msg, MessageSeverity severity,
        @Nullable Throwable cause) {
        return newAtTop(resource, msg, MessageType.ANALYSIS, severity, cause);
    }

    public static Message newAnalysisErrorAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.ANALYSIS, cause);
    }

    public static Message newAnalysisWarningAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.ANALYSIS, cause);
    }

    public static Message newAnalysisNoteAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newNoteAtTop(resource, msg, MessageType.ANALYSIS, cause);
    }


    public static Message newBuilderErrorAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.TRANSFORMATION, cause);
    }

    public static Message newBuilderWarningAtTop(@Nullable FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.TRANSFORMATION, cause);
    }
}
