package org.metaborg.spoofax.core.parser.jsglr;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.parser.ParseResult;

public interface IFileParser<T> {
    public ParseResult<T> parse() throws IOException;

    public IParserConfig getConfig();

    public FileObject getFile();
}
