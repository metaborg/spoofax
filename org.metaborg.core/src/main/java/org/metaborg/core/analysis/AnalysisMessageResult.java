package org.metaborg.core.analysis;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;

public class AnalysisMessageResult {
    public final FileObject source;
    public final Iterable<IMessage> messages;


    public AnalysisMessageResult(FileObject source, Iterable<IMessage> messages) {
        this.source = source;
        this.messages = messages;
    }


    @Override public String toString() {
        return source.toString();
    }
}
