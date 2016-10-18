package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.nabl2.context.IScopeGraphUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IMultiFileScopeGraphUnit extends IScopeGraphUnit {

    void setPartialAnalysis(IStrategoTerm partialAnalysis);

    void reset();

}
