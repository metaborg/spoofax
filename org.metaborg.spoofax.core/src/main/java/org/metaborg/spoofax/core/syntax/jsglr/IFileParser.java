package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.syntax.ParseResult;

public interface IFileParser<T> {
    public ParseResult<T> parse() throws IOException;

    public IParserConfig getConfig();

    public FileObject getFile();
}
