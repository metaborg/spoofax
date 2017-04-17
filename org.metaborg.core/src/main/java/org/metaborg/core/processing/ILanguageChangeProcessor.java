package org.metaborg.core.processing;

import org.metaborg.core.language.LanguageComponentChange;
import org.metaborg.core.language.LanguageImplChange;

/**
 * Interface for processing language change events. Used internally only, clients should use a {@link IProcessorRunner}
 * which handles language change events automatically.
 */
public interface ILanguageChangeProcessor {
    /**
     * Process given language component change event.
     * 
     * @param change
     *            Language implementation component event to process.
     */
    void processComponentChange(LanguageComponentChange change);

    /**
     * Process given language implementation change event.
     * 
     * @param change
     *            Language implementation change event to process.
     */
    void processImplChange(LanguageImplChange change);
}
