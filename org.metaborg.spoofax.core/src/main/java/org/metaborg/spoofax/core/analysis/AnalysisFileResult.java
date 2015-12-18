package org.metaborg.spoofax.core.analysis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.resource.ResourceService;
import org.metaborg.spoofax.core.syntax.ParseResult;

public class AnalysisFileResult<ParseT, AnalysisT> implements Serializable {
    private static final long serialVersionUID = 1497969362814366933L;

    public final @Nullable AnalysisT result;
    public transient FileObject source;
    public final Iterable<IMessage> messages;
    public final ParseResult<ParseT> previous;


    public AnalysisFileResult(@Nullable AnalysisT result, FileObject source, Iterable<IMessage> messages,
        ParseResult<ParseT> previous) {
        this.previous = previous;
        this.source = source;
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
