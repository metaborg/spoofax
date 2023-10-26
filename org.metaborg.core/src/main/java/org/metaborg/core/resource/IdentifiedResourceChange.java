package org.metaborg.core.resource;

import jakarta.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.language.dialect.IdentifiedDialect;

/**
 * Resource change with an identified language and dialect.
 */
public class IdentifiedResourceChange {
    /**
     * Resource change.
     */
    public final ResourceChange change;

    /**
     * Identified dialect of the resource change, or null if it does not have a dialect.
     */
    public final @Nullable ILanguageImpl dialect;

    /**
     * Identified language of the resource change.
     */
    public final ILanguageImpl language;


    public IdentifiedResourceChange(ResourceChange change, IdentifiedDialect identifiedDialect) {
        this(change, identifiedDialect.dialect, identifiedDialect.base);
    }

    public IdentifiedResourceChange(ResourceChange change, IdentifiedResource identifiedResource) {
        this(change, identifiedResource.dialect, identifiedResource.language);
    }

    public IdentifiedResourceChange(ResourceChange change, @Nullable ILanguageImpl dialect, ILanguageImpl language) {
        this.change = change;
        this.language = language;
        this.dialect = dialect;
    }


    /**
     * @return Dialect of the resource change, or the language if it does not belong to a dialect.
     */
    public ILanguageImpl dialectOrLanguage() {
        return dialect == null ? language : dialect;
    }


    @Override public String toString() {
        return change.toString() + " of " + dialectOrLanguage().toString();
    }
}
