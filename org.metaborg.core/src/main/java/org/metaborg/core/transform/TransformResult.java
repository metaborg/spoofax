package org.metaborg.core.transform;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.IResult;
import org.metaborg.core.action.TransformActionContribution;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.util.iterators.Iterables2;

public class TransformResult<V, T> implements IResult<T> {
    public final T result;
    public final Iterable<IMessage> messages;
    public final FileObject source;
    public final @Nullable FileObject output;
    public final IContext context;
    public final long duration;
    public final TransformActionContribution action;
    public final IResult<V> prevResult;


    public TransformResult(T result, Iterable<IMessage> messages, FileObject source, @Nullable FileObject output,
        IContext context, long duration, TransformActionContribution action, IResult<V> prevResult) {
        this.result = result;
        this.messages = messages;
        this.source = source;
        this.output = output;
        this.context = context;
        this.duration = duration;
        this.action = action;
        this.prevResult = prevResult;
    }

    public TransformResult(T result, FileObject source, @Nullable FileObject output, IContext context, long duration,
        TransformActionContribution action, IResult<V> prevResult) {
        this(result, Iterables2.<IMessage>empty(), source, output, context, duration, action, prevResult);
    }
    
    public TransformResult(TransformResult<?, T> result, IResult<V> prevResult) {
        this(result.result, result.source, result.output, result.context, result.duration, result.action, prevResult);
    }


    @Override public T value() {
        return result;
    }

    @Override public Iterable<IMessage> messages() {
        return messages;
    }

    @Override public long duration() {
        return duration;
    }
}
