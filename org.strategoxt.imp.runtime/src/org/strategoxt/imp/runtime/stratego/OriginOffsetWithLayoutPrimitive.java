package org.strategoxt.imp.runtime.stratego;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginOffsetWithLayoutPrimitive extends AbstractOriginPrimitive {

	public OriginOffsetWithLayoutPrimitive() {
		super("SSL_EXT_origin_offset_with_layout");
	}

	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		ITermFactory factory = env.getFactory();
		int start = TextPositions.getStartPosNodeWithLayout(node.getNode());
		int end =  TextPositions.getEndPosNodeWithLayout(node.getNode());
		return factory.makeTuple(
				factory.makeInt(start),
				factory.makeInt(end)
		);
	}

}
