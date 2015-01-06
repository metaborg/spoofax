package org.metaborg.spoofax.core.syntax.jsglr;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.jsglr.client.ParseTable;

public interface IParseTableProvider {
    public ParseTable parseTable();

    public FileObject file();
}
