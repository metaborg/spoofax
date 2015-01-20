package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SourceCategorizerService implements ISourceCategorizerService<IStrategoTerm, IStrategoTerm> {
    @Override public Iterable<ICategory<IStrategoTerm>> categorize(ILanguage language,
        ParseResult<IStrategoTerm> parseResult) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public Iterable<ICategory<IStrategoTerm>> categorize(ILanguage language,
        AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult) {
        // TODO Auto-generated method stub
        return null;
    }
}
