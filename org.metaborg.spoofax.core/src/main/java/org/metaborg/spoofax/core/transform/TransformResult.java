package org.metaborg.spoofax.core.transform;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.messages.IMessage;

public class TransformResult<PrevT, TransT> {
    public final TransT result;
    public final Iterable<IMessage> messages;
    public final Iterable<FileObject> sources;
    public final ILanguage transformedWith;
    public final long duration;
    public final PrevT previousResult;


    public TransformResult(TransT result, Iterable<IMessage> messages, Iterable<FileObject> sources,
        ILanguage transformedWith, long duration, PrevT previousResult) {
        this.result = result;
        this.messages = messages;
        this.sources = sources;
        this.transformedWith = transformedWith;
        this.duration = duration;
        this.previousResult = previousResult;
    }
}
