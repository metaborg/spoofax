package org.metaborg.core.project;

import org.metaborg.core.MetaborgException;

public class ProjectException extends MetaborgException {
    private static final long serialVersionUID = 300295066638212450L;


    public ProjectException() {
    }

    public ProjectException(String message) {
        super(message);
    }

    public ProjectException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProjectException(Throwable cause) {
        super(cause);
    }
}
