package org.metaborg.spoofax.core.analysis;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.context.IContext;

public class AnalysisException extends Exception {
    private static final long serialVersionUID = -6083126502637234259L;

    public final Iterable<FileObject> resources;
    public final IContext context;


    public AnalysisException(Iterable<FileObject> resources, IContext context) {
        super();
        this.resources = resources;
        this.context = context;
    }

    public AnalysisException(Iterable<FileObject> resources, IContext context, String message) {
        super(message);
        this.resources = resources;
        this.context = context;
    }

    public AnalysisException(Iterable<FileObject> resources, IContext context, Throwable cause) {
        super(cause);
        this.resources = resources;
        this.context = context;
    }

    public AnalysisException(Iterable<FileObject> resources, IContext context, String message, Throwable cause) {
        super(message, cause);
        this.resources = resources;
        this.context = context;
    }
}
