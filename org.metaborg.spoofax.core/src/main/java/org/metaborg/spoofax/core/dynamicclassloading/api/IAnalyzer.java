package org.metaborg.spoofax.core.dynamicclassloading.api;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.HybridInterpreter;

public interface IAnalyzer {
    IStrategoTerm analyze(HybridInterpreter runtime, IStrategoTerm inputTerm);
    interface Generated extends IAnalyzer {}
}
