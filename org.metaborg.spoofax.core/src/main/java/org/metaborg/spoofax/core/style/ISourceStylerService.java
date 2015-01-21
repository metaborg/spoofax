package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.language.ILanguage;

public interface ISourceStylerService<ParseT, AnalysisT> {
    public abstract Iterable<IRegionStyle<ParseT>> styleParsed(ILanguage language,
        Iterable<IRegionCategory<ParseT>> categorization);

    public abstract Iterable<IRegionStyle<AnalysisT>> styleAnalyzed(ILanguage language,
        Iterable<IRegionCategory<AnalysisT>> categorization);
}
