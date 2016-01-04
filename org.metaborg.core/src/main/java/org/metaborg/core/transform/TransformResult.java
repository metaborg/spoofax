package org.metaborg.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;

public class TransformResult<V, T> {
    public final T result;
    public final Iterable<IMessage> messages;
    public final Iterable<FileObject> sources;
    public final IContext context;
    public final long duration;
    public final V previousResult;


    public TransformResult(T result, Iterable<IMessage> messages, Iterable<FileObject> sources, IContext context,
        long duration, V previousResult) {
        this.result = result;
        this.messages = messages;
        this.sources = sources;
        this.context = context;
        this.duration = duration;
        this.previousResult = previousResult;
    }
}
