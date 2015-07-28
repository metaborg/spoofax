package org.metaborg.core.analysis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.core.syntax.ParseResult;

public class AnalysisFileResult<P, A> implements Serializable {
    private static final long serialVersionUID = 1497969362814366933L;

    public final @Nullable A result;
    public transient FileObject source;
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ResourceService.writeFileObject(source, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        source = ResourceService.readFileObject(in);
    }
}
