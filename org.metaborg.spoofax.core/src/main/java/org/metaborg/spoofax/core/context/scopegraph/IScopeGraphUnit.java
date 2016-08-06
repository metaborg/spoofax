package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphUnit {

    /** Source resource of this unit */
    String source();


    /** Set meta data associated with an AST node of this unit */
    void setMetadata(int nodeId, IStrategoTerm key, IStrategoTerm value);

    /** Get meta data associated with an AST node of this unit */
    @Nullable IStrategoTerm metadata(int nodeId, IStrategoTerm key);
 

    void setInitial(IStrategoTerm result);

    @Nullable IStrategoTerm initial();

    void setResult(IStrategoTerm result);

    @Nullable IStrategoTerm result();


    /** Reset this unit */
    void reset();

}
