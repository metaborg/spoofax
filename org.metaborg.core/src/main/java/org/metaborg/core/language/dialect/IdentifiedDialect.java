package org.metaborg.core.language.dialect;

import org.metaborg.core.language.ILanguage;

/**
 * Dialect with its base language.
 */
public class IdentifiedDialect {
    /**
     * The dialect.
     */
    public final ILanguage dialect;

    /**
     * Base language of the dialect.
     */
    public final ILanguage base;


    public IdentifiedDialect(ILanguage dialect, ILanguage base) {
        this.dialect = dialect;
        this.base = base;
    }
}
