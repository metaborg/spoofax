package org.metaborg.spoofax.core.service.syntax;

import java.util.Set;

import org.apache.commons.vfs2.FileName;
import org.metaborg.spoofax.core.language.ILanguageFacet;

public class SyntaxFacet implements ILanguageFacet {
    private final FileName parseTable;
    private final Set<String> startSymbols;


    public SyntaxFacet(FileName parseTable, Set<String> startSymbols) {
        this.parseTable = parseTable;
        this.startSymbols = startSymbols;
    }


    public FileName parseTable() {
        return parseTable;
    }

    public Set<String> startSymbols() {
        return startSymbols;
    }
}
