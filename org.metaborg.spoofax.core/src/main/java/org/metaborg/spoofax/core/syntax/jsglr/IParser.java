package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;

import org.metaborg.spoofax.core.syntax.ParseResult;

public interface IParser<T> {
    public ParseResult<T> parse() throws IOException;
}
