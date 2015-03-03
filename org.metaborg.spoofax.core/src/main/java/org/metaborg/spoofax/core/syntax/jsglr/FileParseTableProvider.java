package org.metaborg.spoofax.core.syntax.jsglr;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.spoofax.jsglr.client.ParseTable;
import org.spoofax.jsglr.io.ParseTableManager;

public class FileParseTableProvider implements IParseTableProvider {
    private final FileObject file;
    private final ParseTableManager parseTableManager;
    private final boolean caching;

    private ParseTable cachedTable;


    public FileParseTableProvider(FileObject file, ParseTableManager parseTableManager) {
        this(file, parseTableManager, true);
    }

    public FileParseTableProvider(FileObject file, ParseTableManager parseTableManager, boolean caching) {
        this.file = file;
        this.parseTableManager = parseTableManager;
        this.caching = caching;
    }


    @Override public ParseTable parseTable() throws IOException {
        if(this.cachedTable != null)
            return this.cachedTable;

        if(!file.exists())
            throw new IOException("Could not load parse table, file does not exist");

        final ParseTable table;
        try(final InputStream stream = file.getContent().getInputStream()) {
            table = parseTableManager.loadFromStream(stream);
        } catch(Exception e) {
            throw new IOException("Could not load parse table", e);
        }

        if(caching) {
            this.cachedTable = table;
        }

        return table;
    }

    @Override public String toString() {
        return file.getName().toString();
    }
}
