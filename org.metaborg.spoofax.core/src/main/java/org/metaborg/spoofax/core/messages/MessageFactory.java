package org.metaborg.spoofax.core.messages;

import org.apache.commons.vfs2.FileObject;

public class MessageFactory {
    public static Message newMessage(FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity, MessageType type) {
        return new Message(msg, severity, type, resource, region, null);
    }


    public static Message newParseMessage(FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity) {
        return newMessage(resource, region, msg, MessageSeverity.ERROR, MessageType.PARSER_MESSAGE);
    }

    public static Message newParseError(FileObject resource, ISourceRegion region, String msg) {
        return newParseMessage(resource, region, msg, MessageSeverity.ERROR);
    }

    public static Message newParseWarning(FileObject resource, ISourceRegion region, String msg) {
        return newParseMessage(resource, region, msg, MessageSeverity.WARNING);
    }


    public static Message newAnalysisMessage(FileObject resource, ISourceRegion region, String msg,
        MessageSeverity severity) {
        return newMessage(resource, region, msg, severity, MessageType.ANALYSIS_MESSAGE);
    }

    public static Message newAnalysisError(FileObject resource, ISourceRegion region, String msg) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.ERROR);
    }

    public static Message newAnalysisWarning(FileObject resource, ISourceRegion region, String msg) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.WARNING);
    }

    public static Message newAnalysisNote(FileObject resource, ISourceRegion region, String msg) {
        return newAnalysisMessage(resource, region, msg, MessageSeverity.NOTE);
    }


    private static Message newAtTop(FileObject resource, String msg, MessageType type,
        MessageSeverity severity) {
        return new Message(msg, severity, type, resource, new SourceRegion(0, 0, 0, 0, 0, 0), null);
    }


    private static Message newErrorAtTop(FileObject resource, String msg, MessageType type) {
        return newAtTop(resource, msg, type, MessageSeverity.ERROR);
    }

    private static Message newWarningAtTop(FileObject resource, String msg, MessageType type) {
        return newAtTop(resource, msg, type, MessageSeverity.WARNING);
    }


    public static Message newParseErrorAtTop(FileObject resource, String msg) {
        return newErrorAtTop(resource, msg, MessageType.PARSER_MESSAGE);
    }

    public static Message newParseWarningAtTop(FileObject resource, String msg) {
        return newWarningAtTop(resource, msg, MessageType.PARSER_MESSAGE);
    }


    public static Message newAnalysisMessageAtTop(FileObject resource, String msg, MessageSeverity severity) {
        return newAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE, severity);
    }

    public static Message newAnalysisErrorAtTop(FileObject resource, String msg) {
        return newErrorAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE);
    }

    public static Message newAnalysisWarningAtTop(FileObject resource, String msg) {
        return newWarningAtTop(resource, msg, MessageType.ANALYSIS_MESSAGE);
    }


    public static Message newBuilderErrorAtTop(FileObject resource, String msg) {
        return newErrorAtTop(resource, msg, MessageType.BUILDER_MESSAGE);
    }

    public static Message newBuilderWarningAtTop(FileObject resource, String msg) {
        return newWarningAtTop(resource, msg, MessageType.BUILDER_MESSAGE);
    }
}
