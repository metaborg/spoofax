package org.metaborg.spoofax.core.analysis;

import javax.inject.Inject;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

import com.google.inject.assistedinject.Assisted;

public class StrategoAnalysisFacet implements IAnalysisFacet {
    public final String strategyName;

    public final IStrategoCommon common;

    @Inject public StrategoAnalysisFacet(IStrategoCommon common, @Assisted String strategyName) {
        this.common = common;
        this.strategyName = strategyName;
    }


    @Override public IStrategoTerm analyze(HybridInterpreter runtime, IStrategoTerm inputTerm,
        ILanguageComponent contributor) throws MetaborgException {
        return common.invoke(runtime, inputTerm, strategyName);
    }
}
