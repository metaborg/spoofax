package org.metaborg.core.syntax;

import java.io.Serializable;

public class FenceCharacters implements Serializable {
    private static final long serialVersionUID = 8335721588273074820L;

    public final String open;
    public final String close;


    public FenceCharacters(String open, String close) {
        this.open = open;
        this.close = close;
    }
}
