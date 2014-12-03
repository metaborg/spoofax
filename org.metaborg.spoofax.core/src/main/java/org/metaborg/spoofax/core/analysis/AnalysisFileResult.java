package org.metaborg.spoofax.core.analysis;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.parser.ParseResult;

public class AnalysisFileResult<ParseT, AnalysisT> {
    private final Collection<IMessage> messages = new LinkedList<IMessage>();
    private FileObject file;
    private ParseResult<ParseT> previous;
    private AnalysisT result;

    public AnalysisFileResult(ParseResult<ParseT> previous, FileObject f, Collection<IMessage> messages,
        AnalysisT result) {
        this.previous = previous;
        this.file = f;
        this.result = result;
        this.messages.addAll(messages);
    }

    public Collection<IMessage> messages() {
        return this.messages;
    }

    public AnalysisT result() {
        return result;
    }

    public FileObject file() {
        return file;
    }

    public ParseResult<ParseT> parseResult() {
        return previous;
    }
}
