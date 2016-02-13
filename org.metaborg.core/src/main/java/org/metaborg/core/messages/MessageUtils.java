package org.metaborg.core.messages;

public class MessageUtils {
    public static MessageSeverity highestSeverity(Iterable<IMessage> messages) {
        MessageSeverity maxSeverity = MessageSeverity.NOTE;
        for(IMessage message : messages) {
            final MessageSeverity severity = message.severity();
            if(severity.value > maxSeverity.value) {
                maxSeverity = severity;
            }
        }
        return maxSeverity;
    }

    public static boolean containsSeverity(Iterable<IMessage> messages, MessageSeverity severity) {
        for(IMessage message : messages) {
            if(message.severity().equals(severity)) {
                return true;
            }
        }
        return false;
    }
}
