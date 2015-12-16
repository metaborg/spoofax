package org.metaborg.core.analysis;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.ParseResult;

public class AnalysisFileResult<P, A> {
    public final @Nullable A result;
    public final FileObject source;
    public final IContext context;
    public final Iterable<IMessage> messages;
    public final ParseResult<P> previous;


    public AnalysisFileResult(@Nullable A result, FileObject source, IContext context, Iterable<IMessage> messages,
        ParseResult<P> previous) {
        this.previous = previous;
        this.source = source;
        this.context = context;
        this.result = result;
        this.messages = messages;
    }
}
