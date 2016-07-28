package org.metaborg.spoofax.core.stratego.primitives.scopegraph;

import org.metaborg.core.MetaborgException;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.metaborg.spoofax.core.context.scopegraph.ScopeGraphUtil;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class SG_set_ast_metadata extends ScopeGraphPrimitive {
    
    @Inject public SG_set_ast_metadata() {
        super(SG_set_ast_metadata.class.getSimpleName(), 0, 2);
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
            throw new InterpreterException("Cannot set metadata for a resource that is not being analyzed.");
        }
        unit.setMetadata(astIndex.index, terms[0], terms[1]);
        return true;
    }

}