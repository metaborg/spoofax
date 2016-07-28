package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUtil;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SG_get_ast_metadata extends ScopeGraphPrimitive {
    
    public SG_get_ast_metadata() {
        super(SG_get_ast_metadata.class.getSimpleName(), 0, 1);
    }


    @Override public boolean call(IScopeGraphContext context, IContext env,
            Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        ASTIndex astIndex;
        try {
            astIndex = ScopeGraphUtil.getASTIndex(env.current());
        } catch(MetaborgException ex) {
            throw new InterpreterException(ex);
        }
        IScopeGraphUnit unit = context.unit(astIndex.source);
        if(unit == null) {
            return false;
        }
        IStrategoTerm value = unit.metadata(astIndex.index, terms[0]);
        if(value == null) {
            return false;
        }
        env.setCurrent(value);
        return true;
    }

}
