package org.metaborg.core.build.dependency;

import org.metaborg.core.MetaborgException;

/**
 * Thrown when a dependency is not found or not loaded.
 */
public class MissingDependencyException extends MetaborgException {
    private static final long serialVersionUID = -2433147675712812448L;

    private static final String DefaultMessage = "Dependency not found or not loaded.";


    /**
     * Initializes a new instance of the {@link MissingDependencyException} class.
     *
     * @param message
     *            The error message; or <code>null</code>.
     * @param cause
     *            The exception cause; or <code>null</code>.
     */
    public MissingDependencyException(String message, Throwable cause) {
        super(message != null ? message : DefaultMessage, cause);
    }

    /**
     * Initializes a new instance of the {@link MissingDependencyException} class.
     *
     * @param message
     *            The error message; or <code>null</code>.
     */
    public MissingDependencyException(String message) {
        this(message, null);
    }

    /**
     * Initializes a new instance of the {@link MissingDependencyException} class.
     *
     * @param cause
     *            The exception cause; or <code>null</code>.
     */
    public MissingDependencyException(Throwable cause) {
        this(null, cause);
    }

    /**
     * Initializes a new instance of the {@link MissingDependencyException} class.
     */
    public MissingDependencyException() {
        this(null, null);
    }
}
