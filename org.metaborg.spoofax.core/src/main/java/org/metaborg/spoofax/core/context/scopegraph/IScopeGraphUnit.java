package org.metaborg.spoofax.core.context.scopegraph;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

public interface IScopeGraphUnit {

    /** Source resource of this unit */
    String source();

    /** Parse unit of this unit */
    @Nullable ISpoofaxParseUnit parseUnit();


    /** Set generated constraint for this unit */
    void setConstraint(IStrategoTerm constraint);

    /** Get generated constraint for this unit */
    @Nullable IStrategoTerm constraint();


    /** Set meta data associated with an AST node of this unit */
    void setMetadata(IStrategoTerm node, IStrategoTerm key, IStrategoTerm value);

    /** Get meta data associated with an AST node of this unit */
    @Nullable IStrategoTerm metadata(IStrategoTerm node, IStrategoTerm key);
 

    /** Set analysis result of this unit */  
    void setAnalysis(IStrategoTerm analysis);

    /** Get analysis result of this unit */  
    @Nullable IStrategoTerm analysis();


    /** Reset this unit */
    void reset();
 
}
