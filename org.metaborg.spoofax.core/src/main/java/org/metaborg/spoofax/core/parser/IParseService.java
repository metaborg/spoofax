package org.metaborg.spoofax.core.parser;

import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public interface IParseService<T> {
    public ParseResult<T> parse(FileObject object, ILanguage language) throws IOException;
}
