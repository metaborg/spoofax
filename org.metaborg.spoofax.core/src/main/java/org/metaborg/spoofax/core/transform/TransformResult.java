package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.messages.IMessage;

public class TransformResult<PrevT, TransT> {
    public final TransT result;
    public final Iterable<IMessage> messages;
    public final Iterable<FileObject> sources;
    public final IContext context;
    public final long duration;
    public final PrevT previousResult;


    public TransformResult(TransT result, Iterable<IMessage> messages, Iterable<FileObject> sources, IContext context,
        long duration, PrevT previousResult) {
        this.result = result;
        this.messages = messages;
        this.sources = sources;
        this.context = context;
        this.duration = duration;
        this.previousResult = previousResult;
    }
}
