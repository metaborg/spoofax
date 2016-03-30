package org.metaborg.spoofax.core.unit;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.messages.IMessage;

public class AnalyzeUnitUpdate implements ISpoofaxAnalyzeUnitUpdate {
    private final FileObject source;
    private final AnalyzeUpdateData data;


    public AnalyzeUnitUpdate(FileObject source, AnalyzeUpdateData data) {
        this.source = source;
        this.data = data;
    }


    @Override public FileObject source() {
        return source;
    }

    @Override public Iterable<IMessage> messages() {
        return data.messages;
    }
}
