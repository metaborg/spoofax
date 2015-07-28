package org.metaborg.core.transform;

import org.metaborg.core.MetaborgException;

public class TransformerException extends MetaborgException {
    private static final long serialVersionUID = -9086897694888391754L;


    public TransformerException(String msg) {
        super(msg);
    }

    public TransformerException(String msg, Throwable t) {
        super(msg, t);
    }

    public TransformerException(Throwable t) {
        super(t);
    }
}
