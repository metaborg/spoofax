package org.metaborg.spoofax.core.parser.jsglr;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.jsglr.client.ParseTable;

public interface IParseTableProvider {
    public ParseTable parseTable();

    public FileObject file();
}
