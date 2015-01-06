package org.metaborg.spoofax.core.messages;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.collection.HashMultiTable;
import org.metaborg.util.collection.MultiTable;

import com.google.inject.Inject;

public class MessageService implements IMessageService {
    private final MultiTable<FileObject, MessageCategory, IMessage> messages = HashMultiTable.create();


    @Inject public MessageService() {

    }


    @Override public Iterable<IMessage> get() {
        return messages.values();
    }

    @Override public Iterable<IMessage> get(FileObject resource) {
        return messages.rowValues(resource);
    }

    @Override public Iterable<IMessage> get(MessageCategory category) {
        return messages.columnValues(category);
    }

    @Override public Iterable<IMessage> get(FileObject resource, MessageCategory category) {
        return messages.get(resource, category);
    }

    @Override public void add(IMessage message, MessageCategory category) {
        messages.put(message.source(), category, message);
    }

    @Override public void clear(FileObject resource, MessageCategory category) {
        messages.get(resource, category).clear();
    }
}
