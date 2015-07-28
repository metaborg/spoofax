package org.metaborg.core.transform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.ResourceService;

import com.google.common.collect.Lists;

public class TransformResult<PrevT, TransT> implements Serializable {
    private static final long serialVersionUID = 9088183760418269222L;

    public final TransT result;
    public final Iterable<IMessage> messages;
    public transient Iterable<FileObject> sources;
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


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        List<FileObject> ctreeList = Lists.newArrayList(sources);
        out.writeInt(ctreeList.size());
        for(FileObject fo : ctreeList)
            ResourceService.writeFileObject(fo, out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        List<FileObject> sourceList = Lists.newArrayList();
        int sourceCount = in.readInt();
        for(int i = 0; i < sourceCount; i++)
            sourceList.add(ResourceService.readFileObject(in));
        this.sources = sourceList;
    }
}
