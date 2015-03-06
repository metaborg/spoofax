package org.metaborg.spoofax.core.analysis;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.syntax.ParseResult;

public class AnalysisFileResult<ParseT, AnalysisT> {
    public final @Nullable AnalysisT result;
    public final FileObject source;
    public final Iterable<IMessage> messages;
    public final ParseResult<ParseT> previous;


    public AnalysisFileResult(@Nullable AnalysisT result, FileObject source, Iterable<IMessage> messages,
        ParseResult<ParseT> previous) {
        this.previous = previous;
        this.source = source;
        this.result = result;
        this.messages = messages;
    }
}
