package org.metaborg.core.analysis;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;

/**
 * Exception indicating that analysis failed unexpectedly.
 */
public class AnalysisException extends MetaborgException {
    private static final long serialVersionUID = -6083126502637234259L;

    public final IContext context;


    public AnalysisException(IContext context) {
        super();
        this.context = context;
    }

    public AnalysisException(IContext context, String message) {
        super(message);
        this.context = context;
    }

    public AnalysisException(IContext context, Throwable cause) {
        super(cause);
        this.context = context;
    }

    public AnalysisException(IContext context, String message, Throwable cause) {
        super(message, cause);
        this.context = context;
    }
}
