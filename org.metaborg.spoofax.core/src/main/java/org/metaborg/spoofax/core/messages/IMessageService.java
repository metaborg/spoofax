package org.metaborg.spoofax.core.messages;

import org.apache.commons.vfs2.FileObject;

public interface IMessageService {
    public Iterable<IMessage> get();

    public Iterable<IMessage> get(FileObject resource);

    public Iterable<IMessage> get(MessageCategory category);

    public Iterable<IMessage> get(FileObject resource, MessageCategory category);

    public void add(IMessage message, MessageCategory category);

    public void clear(FileObject resource, MessageCategory category);
}
