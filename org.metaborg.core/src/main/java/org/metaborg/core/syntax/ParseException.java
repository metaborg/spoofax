package org.metaborg.core.syntax;

import org.metaborg.core.MetaborgException;

/**
 * Exception indicating that parsing failed unexpectedly.
 */
public class ParseException extends MetaborgException {
    private static final long serialVersionUID = 794040128416462015L;

    public final IInputUnit unit;


    public ParseException(IInputUnit unit) {
        super();
        this.unit = unit;
    }

    public ParseException(IInputUnit unit, String message) {
        super(message);
        this.unit = unit;
    }

    public ParseException(IInputUnit unit, Throwable cause) {
        super(cause);
        this.unit = unit;
    }

    public ParseException(IInputUnit unit, String message, Throwable cause) {
        super(message, cause);
        this.unit = unit;
    }
}
