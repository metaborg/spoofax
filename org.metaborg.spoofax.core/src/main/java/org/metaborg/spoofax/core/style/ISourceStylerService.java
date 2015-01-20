package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.language.ILanguage;

public interface ISourceStylerService<ParseT, AnalysisT> {
    public abstract Iterable<IStyle<ParseT>> styleParsed(ILanguage language,
        Iterable<ICategory<ParseT>> categorization);

    public abstract Iterable<IStyle<AnalysisT>> styleAnalyzed(ILanguage language,
        Iterable<ICategory<AnalysisT>> categorization);
}
