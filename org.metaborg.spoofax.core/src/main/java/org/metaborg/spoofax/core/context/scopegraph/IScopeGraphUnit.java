package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphUnit {

    /** Source resource of this unit */
    String source();

    /** Parse unit of this unit */
    @Nullable ISpoofaxParseUnit parseUnit();


    /** Set meta data associated with an AST node of this unit */
    void setMetadata(int nodeId, IStrategoTerm key, IStrategoTerm value);

    /** Get meta data associated with an AST node of this unit */
    @Nullable IStrategoTerm metadata(int nodeId, IStrategoTerm key);
 

    void setInitialResult(IStrategoTerm result);

    @Nullable IStrategoTerm initialResult();

    void setUnitResult(IStrategoTerm result);

    @Nullable IStrategoTerm unitResult();

    void setFinalResult(IStrategoTerm result);

    @Nullable IStrategoTerm finalResult();


    /** Reset this unit */
    void reset();

}
