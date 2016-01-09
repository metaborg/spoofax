package org.metaborg.core.syntax;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;

public class ParseException extends MetaborgException {
    private static final long serialVersionUID = 794040128416462015L;

    public final @Nullable FileObject resource;
    public final ILanguageImpl language;


    public ParseException(@Nullable FileObject resource, ILanguageImpl language) {
        super();
        this.resource = resource;
        this.language = language;
    }

    public ParseException(@Nullable FileObject resource, ILanguageImpl language, String message) {
        super(message);
        this.resource = resource;
        this.language = language;
    }

    public ParseException(@Nullable FileObject resource, ILanguageImpl language, Throwable cause) {
        super(cause);
        this.resource = resource;
        this.language = language;
    }

    public ParseException(@Nullable FileObject resource, ILanguageImpl language, String message, Throwable cause) {
        super(message, cause);
        this.resource = resource;
        this.language = language;
    }
}
