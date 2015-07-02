package org.metaborg.core.syntax;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.SpoofaxException;
import org.metaborg.core.language.ILanguage;

public class ParseException extends SpoofaxException {
    private static final long serialVersionUID = 794040128416462015L;

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
