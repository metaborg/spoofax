package org.metaborg.spoofax.core.syntax;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;

public class ParseException extends Exception {
    private static final long serialVersionUID = -3971919541233400584L;

    public final FileObject resource;
    public final ILanguage language;


    public ParseException(FileObject resource, ILanguage language) {
        super();
        this.resource = resource;
        this.language = language;
    }

    public ParseException(FileObject resource, ILanguage language, String message) {
        super(message);
        this.resource = resource;
        this.language = language;
    }

    public ParseException(FileObject resource, ILanguage language, Throwable cause) {
        super(cause);
        this.resource = resource;
        this.language = language;
    }

    public ParseException(FileObject resource, ILanguage language, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.language = language;
    }
}
