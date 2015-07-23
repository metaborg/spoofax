package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageImplChange;

/**
 * Interface for processing language change events. Used internally only, clients should use a {@link IProcessorRunner}
 * which handles language change events automatically.
 */
public interface ILanguageChangeProcessor {
    /**
     * Process given language change event.
     * 
     * @param change
     *            Language implementation change event to process.
     * @param progressReporter
     *            Progress reporter, or null to use a processor-specific implementation for progress reporting.
     */
    public abstract void process(LanguageImplChange change, @Nullable IProgressReporter progressReporter);
}
