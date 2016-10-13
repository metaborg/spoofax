package org.metaborg.spoofax.core.context.scopegraph;

import org.metaborg.scopegraph.context.IScopeGraphUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IMultiFileScopeGraphUnit extends IScopeGraphUnit {

    void setPartialAnalysis(IStrategoTerm partialAnalysis);

    void reset();

}
