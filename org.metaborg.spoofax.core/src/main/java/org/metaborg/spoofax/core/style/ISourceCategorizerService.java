package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ParseResult;

public interface ISourceCategorizerService<ParseT, AnalysisT> {
    public abstract Iterable<ICategory<ParseT>>
        categorize(ILanguage language, ParseResult<ParseT> parseResult);

    public abstract Iterable<ICategory<AnalysisT>> categorize(ILanguage language,
        AnalysisFileResult<ParseT, AnalysisT> analysisResult);
}
