package org.metaborg.spoofax.core.unit;

import org.metaborg.core.messages.IMessage;
import org.metaborg.util.iterators.Iterables2;

public class AnalyzeUpdateData {
    public final Iterable<IMessage> messages;


    public AnalyzeUpdateData(Iterable<IMessage> messages) {
        this.messages = messages;
    }

    public AnalyzeUpdateData() {
        this(Iterables2.<IMessage>empty());
    }
}
