package org.metaborg.spoofax.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.language.ILanguage;

public class ContextException extends SpoofaxException {
    private static final long serialVersionUID = -3491537178639412764L;


    public final FileObject resource;
    public final ILanguage language;


    public ContextException(FileObject resource, ILanguage language) {
        super();
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguage language, String message) {
        super(message);
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguage language, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguage language, Throwable cause) {
        super(cause);
        this.resource = resource;
        this.language = language;
    }
}
