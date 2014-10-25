package org.metaborg.spoofax.core.messages;

import org.apache.commons.vfs2.FileObject;

public interface IMessage {
    public String message();

    public MessageSeverity severity();

    public MessageType type();

    public FileObject source();

    public ICodeRegion region();

    public Throwable exception();
}