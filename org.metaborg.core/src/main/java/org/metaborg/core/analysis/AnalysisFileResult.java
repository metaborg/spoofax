package org.metaborg.core.analysis;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.IResult;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.ParseResult;

public class AnalysisFileResult<P, A> implements IResult<A> {
    public final @Nullable A result;
    public final FileObject source;
    public final Iterable<IMessage> messages;
    public final IContext context;
    public final ParseResult<P> previous;


    public AnalysisFileResult(@Nullable A result, FileObject source, IContext context, Iterable<IMessage> messages,
        ParseResult<P> previous) {
        this.previous = previous;
        this.source = source;
        this.context = context;
        this.result = result;
        this.messages = messages;
    }


    @Override public @Nullable A value() {
        return result;
    }

    @Override public Iterable<IMessage> messages() {
        return messages;
    }

    @Override public long duration() {
        return -1;
    }
}
