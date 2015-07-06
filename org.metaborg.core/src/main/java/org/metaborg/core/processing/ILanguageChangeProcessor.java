package org.metaborg.core.processing;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageChange;

public interface ILanguageChangeProcessor {
    public abstract void process(LanguageChange change, @Nullable IProgressReporter progressReporter);
}
