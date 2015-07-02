package org.metaborg.core;

public class MetaborgRuntimeException extends RuntimeException {
    private static final long serialVersionUID = -7668398660885536315L;


    public MetaborgRuntimeException() {
        super();
    }

    public MetaborgRuntimeException(String message) {
        super(message);
    }

    public MetaborgRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaborgRuntimeException(Throwable cause) {
        super(cause);
    }
}
