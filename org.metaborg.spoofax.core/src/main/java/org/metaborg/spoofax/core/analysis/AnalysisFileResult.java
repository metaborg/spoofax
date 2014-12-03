package org.metaborg.spoofax.core.analysis;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.parser.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class AnalysisFileResult {
    private final Collection<IMessage> messages = new LinkedList<IMessage>();
    private FileObject file;
    private ParseResult<IStrategoTerm> previous;
    private IStrategoTerm ast;

    public AnalysisFileResult(ParseResult<IStrategoTerm> previous, FileObject f,
        Collection<IMessage> messages, IStrategoTerm ast) {
        this.previous = previous;
        this.file = f;
        this.ast = ast;
        this.messages.addAll(messages);
    }

    public Collection<IMessage> messages() {
        return this.messages;
    }

    public IStrategoTerm ast() {
        return ast;
    }

    public FileObject file() {
        return file;
    }

    public ParseResult<IStrategoTerm> previousResult() {
        return previous;
    }
}
