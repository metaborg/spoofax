package org.metaborg.spoofax.core.completion;

import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.syntax.ParseResult;

public interface ICompletionService {
    public abstract Iterable<ICompletion> get(ParseResult<?> parseResult, int offset) throws SpoofaxException;
}
