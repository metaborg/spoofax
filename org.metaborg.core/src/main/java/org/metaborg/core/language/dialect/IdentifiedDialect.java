package org.metaborg.core.language.dialect;

import org.metaborg.core.language.ILanguageImpl;

/**
 * Dialect with its base language.
 */
public class IdentifiedDialect {
    /**
     * The dialect.
     */
    public final ILanguageImpl dialect;

    /**
     * Base language of the dialect.
     */
    public final ILanguageImpl base;


    public IdentifiedDialect(ILanguageImpl dialect, ILanguageImpl base) {
        this.dialect = dialect;
        this.base = base;
    }
}
