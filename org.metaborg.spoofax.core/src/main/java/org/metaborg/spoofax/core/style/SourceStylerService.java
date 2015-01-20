package org.metaborg.spoofax.core.style;

import org.metaborg.spoofax.core.language.ILanguage;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SourceStylerService implements ISourceStylerService<IStrategoTerm, IStrategoTerm> {
    @Override public Iterable<IStyle<IStrategoTerm>> styleParsed(ILanguage language,
        Iterable<ICategory<IStrategoTerm>> categorization) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override public Iterable<IStyle<IStrategoTerm>> styleAnalyzed(ILanguage language,
        Iterable<ICategory<IStrategoTerm>> categorization) {
        // TODO Auto-generated method stub
        return null;
    }
}
