package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;

public class ContextException extends MetaborgException {
    private static final long serialVersionUID = -3491537178639412764L;

    public final FileObject resource;
    public final ILanguageImpl language;


    public ContextException(FileObject resource, ILanguageImpl language) {
        super();
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguageImpl language, String message) {
        super(message);
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguageImpl language, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.language = language;
    }

    public ContextException(FileObject resource, ILanguageImpl language, Throwable cause) {
        super(cause);
        this.resource = resource;
        this.language = language;
    }
}
