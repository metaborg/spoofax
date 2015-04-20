package org.metaborg.spoofax.core.messages;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

public interface IMessage extends Serializable {
    public String message();

    public MessageSeverity severity();

    public MessageType type();

    public FileObject source();

    public ISourceRegion region();

    @Nullable public Throwable exception();
}