package org.metaborg.spoofax.core.messages;

public class MessageUtils {
    public static boolean containsSeverity(Iterable<IMessage> messages, MessageSeverity severity) {
        for(IMessage message : messages) {
            if(message.severity().equals(severity)) {
                return true;
            }
        }
        return false;
    }
}
