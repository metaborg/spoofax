package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.parser.ast.AstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginRootPrimitive extends AbstractPrimitive {

private static final String NAME = "SSL_EXT_origin_root";
	
	public OriginRootPrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		AstNode result = EditorState.getActiveEditor().getCurrentAst();
		if (result == null) return false;
		env.setCurrent(result.getTerm());
		return true;
	}

}
