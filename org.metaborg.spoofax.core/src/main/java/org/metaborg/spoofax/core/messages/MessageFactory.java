package org.metaborg.spoofax.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public class MessageFactory {
    public static Message newMessage(FileObject resource, ISourceRegion region, String msg, MessageSeverity severity,
        MessageType type, @Nullable Throwable cause) {
        return new Message(msg, severity, type, resource, region, cause);
    }


    public static Message newParseMessage(FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity, @Nullable Throwable cause) {
        return newMessage(resource, region, msg, severity, MessageType.PARSER_MESSAGE, cause);
    }

    public static Message
        newParseError(FileObject resource, ISourceRegion region, String msg, @Nullable Throwable cause) {
        return newParseMessage(resource, region, msg, MessageSeverity.ERROR, cause);
    }

    public static Message newParseWarning(FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newParseMessage(resource, region, msg, MessageSeverity.WARNING, cause);
    }


    public static Message newAnalysisMessage(FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity, @Nullable Throwable cause) {
        return newMessage(resource, region, msg, severity, MessageType.ANALYSIS_MESSAGE, cause);
    }

    public static Message newAnalysisError(FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.ERROR, cause);
    }

    public static Message newAnalysisWarning(FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.WARNING, cause);
    }

    public static Message newAnalysisNote(FileObject resource, ISourceRegion region, String msg,
        @Nullable Throwable cause) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.NOTE, cause);
    }


    public static Message newAtTop(FileObject resource, String msg, MessageType type, MessageSeverity severity,
        @Nullable Throwable cause) {
        return new Message(msg, severity, type, resource, new SourceRegion(0, 0, 0, 0, 0, 0), cause);
    }

    public static Message newErrorAtTop(FileObject resource, String msg, MessageType type, @Nullable Throwable cause) {
        return newAtTop(resource, msg, type, MessageSeverity.ERROR, cause);
    }

    public static Message newWarningAtTop(FileObject resource, String msg, MessageType type, @Nullable Throwable cause) {
        return newAtTop(resource, msg, type, MessageSeverity.WARNING, cause);
    }


    public static Message newParseErrorAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.PARSER_MESSAGE, cause);
    }

    public static Message newParseWarningAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.PARSER_MESSAGE, cause);
    }


    public static Message newAnalysisMessageAtTop(FileObject resource, String msg, MessageSeverity severity,
        @Nullable Throwable cause) {
        return newAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE, severity, cause);
    }

    public static Message newAnalysisErrorAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE, cause);
    }

    public static Message newAnalysisWarningAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE, cause);
    }


    public static Message newBuilderErrorAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newErrorAtTop(resource, msg, MessageType.BUILDER_MESSAGE, cause);
    }

    public static Message newBuilderWarningAtTop(FileObject resource, String msg, @Nullable Throwable cause) {
        return newWarningAtTop(resource, msg, MessageType.BUILDER_MESSAGE, cause);
    }
}
