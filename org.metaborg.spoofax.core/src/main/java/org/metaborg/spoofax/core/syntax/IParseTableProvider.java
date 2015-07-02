package org.metaborg.spoofax.core.syntax;

import java.io.IOException;

import org.spoofax.jsglr.client.ParseTable;

public interface IParseTableProvider {
    public abstract ParseTable parseTable() throws IOException;
}
