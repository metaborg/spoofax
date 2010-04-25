package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginLayoutPositionPrimitive extends AbstractOriginPrimitive {

	public OriginLayoutPositionPrimitive() {
		super("SSL_EXT_origin_layout_term_pos");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		return node.getNode().getTerm();
	}

}
