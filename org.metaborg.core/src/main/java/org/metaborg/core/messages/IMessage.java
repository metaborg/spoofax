package org.metaborg.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

/**
 * Interface representing a message on a region in a source file.
 */
public interface IMessage {
    /**
     * @return Message text
     */
    public String message();

    /**
     * @return Message severity
     */
    public MessageSeverity severity();

    /**
     * @return Message type
     */
    public MessageType type();

    /**
     * @return Source of the message
     */
    public FileObject source();

    /**
     * @return Affected region inside the source, or null if the entire source is affected.
     */
    public @Nullable ISourceRegion region();

    /**
     * @return Exception belonging to this message, or null if there is no exception.
     */
    public @Nullable Throwable exception();
}
