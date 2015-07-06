package org.metaborg.core.build;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguage;
import org.metaborg.core.resource.IResourceChange;

/**
 * Resource change with an identified language and dialect.
 */
public class IdentifiedResourceChange {
    /**
     * Resource change.
     */
    public final IResourceChange change;

    /**
     * Identified language of the resource change.
     */
    public final ILanguage language;

    /**
     * Identified dialect of the resource change, or null if it does not have a dialect.
     */
    public final @Nullable ILanguage dialect;


    public IdentifiedResourceChange(IResourceChange change, ILanguage language, @Nullable ILanguage dialect) {
        this.change = change;
        this.language = language;
        this.dialect = dialect;
    }


    @Override public String toString() {
        return change.toString() + " in " + language.toString();
    }
}
