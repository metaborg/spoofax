package org.metaborg.core.transform;

import org.metaborg.core.MetaborgException;

public class TransformException extends MetaborgException {
    private static final long serialVersionUID = -9086897694888391754L;


    public TransformException(String msg) {
        super(msg);
    }

    public TransformException(String msg, Throwable t) {
        super(msg, t);
    }

    public TransformException(Throwable t) {
        super(t);
    }
}
