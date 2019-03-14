package org.metaborg.spoofax.core.analysis;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public interface IAnalysisFacet extends IFacet {
    IStrategoTerm analyze(HybridInterpreter runtime, IStrategoTerm inputTerm, ILanguageComponent contributor) throws MetaborgException;
}
