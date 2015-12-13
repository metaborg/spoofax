package org.metaborg.core.syntax;

import org.metaborg.core.language.IFacet;

public class ParseFacet implements IFacet {
    public final String type;


    public ParseFacet(String type) {
        this.type = type;
    }
}
