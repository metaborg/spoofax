package org.metaborg.spoofax.core.transform;

import org.metaborg.spoofax.core.SpoofaxException;

public class TransformerException extends SpoofaxException {
    private static final long serialVersionUID = -1803216442012616216L;


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
