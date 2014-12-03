package org.metaborg.spoofax.core;

public class SpoofaxException extends RuntimeException {
    private static final long serialVersionUID = 1786746541057331233L;

    public SpoofaxException(String msg) {
        super(msg);
    }

    public SpoofaxException(String msg, Throwable t) {
        super(msg, t);
    }

    public SpoofaxException(Throwable t) {
        super(t);
    }
}
