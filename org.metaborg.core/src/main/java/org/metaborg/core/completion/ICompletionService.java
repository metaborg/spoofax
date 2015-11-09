package org.metaborg.core.completion;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.syntax.ParseResult;

public interface ICompletionService<P> {
    public abstract Iterable<ICompletion> get(ParseResult<P> parseResult, int offset) throws MetaborgException;
}
