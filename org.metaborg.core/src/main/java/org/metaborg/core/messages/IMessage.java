package org.metaborg.core.messages;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.source.ISourceRegion;

/**
 * Interface representing a message on a region in a source file.
 */
public interface IMessage {
    /**
     * @return Message text.
     */
    String message();

    /**
     * @return Message severity
     */
    MessageSeverity severity();

    /**
     * @return Message type.
     */
    MessageType type();

    /**
     * @return Source of the message, or null if the source is unknown.
     */
    @Nullable FileObject source();

    /**
     * @return Affected region inside the source, or null if the entire source is affected.
     */
    @Nullable ISourceRegion region();

    /**
     * @return Exception belonging to this message, or null if there is no exception.
     */
    @Nullable Throwable exception();
}
