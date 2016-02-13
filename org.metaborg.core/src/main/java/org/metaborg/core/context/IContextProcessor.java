package org.metaborg.core.context;

import org.metaborg.core.language.LanguageImplChange;

public interface IContextProcessor {
    /**
     * Updates contexts using a language implementation change.
     * 
     * @param change
     *            Language change to process.
     */
    void update(LanguageImplChange change);
}
