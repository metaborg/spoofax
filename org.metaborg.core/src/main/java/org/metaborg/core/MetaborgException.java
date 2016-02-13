package org.metaborg.core;

public class MetaborgException extends Exception {
    private static final long serialVersionUID = -3661402088434126639L;


    public MetaborgException() {
        super();
    }

    public MetaborgException(String message) {
        super(message);
    }

    public MetaborgException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetaborgException(Throwable cause) {
        super(cause);
    }
}
