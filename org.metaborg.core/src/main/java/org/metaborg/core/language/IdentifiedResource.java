package org.metaborg.core.language;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.dialect.IdentifiedDialect;

/**
 * Resource with an identified language and optionally dialect.
 */
public class IdentifiedResource {
    /**
     * Resource.
     */
    public final FileObject resource;

    /**
     * Identified dialect of the resource, or null if it does not have a dialect.
     */
    public final @Nullable ILanguage dialect;

    /**
     * Identified language of the resource.
     */
    public final ILanguage language;


    public IdentifiedResource(FileObject resource, IdentifiedDialect identifiedDialect) {
        this(resource, identifiedDialect.dialect, identifiedDialect.base);
    }

    public IdentifiedResource(FileObject resource, @Nullable ILanguage dialect, ILanguage language) {
        this.resource = resource;
        this.language = language;
        this.dialect = dialect;
    }


    /**
     * @return Dialect of the resource, or the language if it does not belong to a dialect.
     */
    public ILanguage dialectOrLanguage() {
        return dialect == null ? language : dialect;
    }


    @Override public String toString() {
        return resource.toString() + " of " + dialectOrLanguage().toString();
    }
}
