package org.metaborg.spoofax.core;

public class SpoofaxRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -7668398660885536315L;


    public SpoofaxRuntimeException() {
        super();
    }

    public SpoofaxRuntimeException(String message) {
        super(message);
    }

    public SpoofaxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpoofaxRuntimeException(Throwable cause) {
        super(cause);
    }
}
