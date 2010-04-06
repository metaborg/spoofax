package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginTermPrimitive extends AbstractOriginPrimitive {

	public OriginTermPrimitive() {
		super("SSL_EXT_origin_term");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		return node.getNode().getTerm();
	}

}
