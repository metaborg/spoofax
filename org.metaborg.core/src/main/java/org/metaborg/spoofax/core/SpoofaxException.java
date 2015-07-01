package org.metaborg.spoofax.core;

public class SpoofaxException extends Exception {
    private static final long serialVersionUID = -3661402088434126639L;


    public SpoofaxException() {
        super();
    }

    public SpoofaxException(String message) {
        super(message);
    }

    public SpoofaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpoofaxException(Throwable cause) {
        super(cause);
    }
}
